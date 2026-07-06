package com.aidenir.weighttracker.motion

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import com.aidenir.weighttracker.MainActivity
import com.aidenir.weighttracker.R
import com.aidenir.weighttracker.notifications.NotificationChannels
import com.aidenir.weighttracker.notifications.ReminderNotifier
import kotlin.math.sqrt

/**
 * Watches the accelerometer for sustained motion, then fires the weigh-in
 * reminder. Auto-stops after a max window (default 90 min).
 */
class OutOfBedService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var lastMotionMs = 0L
    private var startedAtMs = 0L
    private var restingSamples = 0
    private var motionSamples = 0
    private val movedThresholdMs = 4_000L

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureRegistered(this)
        startedAtMs = SystemClock.elapsedRealtime()
        val pi = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, NotificationChannels.SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_service_title))
            .setContentText(getString(R.string.notif_service_body))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()

        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        } else 0
        ServiceCompat.startForeground(this, NOTIF_ID, notification, serviceType)

        sensorManager = getSystemService()!!
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accel?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: run {
            // Device has no accelerometer — just fire the reminder immediately.
            ReminderNotifier.show(this)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val magnitude = sqrt(
            event.values[0] * event.values[0] +
                event.values[1] * event.values[1] +
                event.values[2] * event.values[2]
        )
        // Subtract gravity (~9.81) to see net movement.
        val net = kotlin.math.abs(magnitude - 9.81f)
        val now = SystemClock.elapsedRealtime()

        if (net > MOTION_THRESHOLD) {
            if (lastMotionMs == 0L) lastMotionMs = now
            motionSamples++
            restingSamples = 0
            if (now - lastMotionMs > movedThresholdMs && motionSamples > 12) {
                fireAndStop()
                return
            }
        } else {
            restingSamples++
            if (restingSamples > 40) {
                // long calm period — reset the accumulator
                motionSamples = 0
                lastMotionMs = 0
            }
        }

        // safety timeout
        if (now - startedAtMs > MAX_WATCH_MS) {
            fireAndStop()
        }
    }

    private fun fireAndStop() {
        ReminderNotifier.show(this)
        stopSelf()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        runCatching { sensorManager.unregisterListener(this) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIF_ID = 1002
        private const val MOTION_THRESHOLD = 0.9f // m/s^2 above gravity
        private val MAX_WATCH_MS = 90L * 60 * 1000

        fun start(context: Context) {
            val intent = Intent(context, OutOfBedService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OutOfBedService::class.java))
        }
    }
}
