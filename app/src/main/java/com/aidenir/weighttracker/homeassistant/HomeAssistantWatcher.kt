package com.aidenir.weighttracker.homeassistant

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aidenir.weighttracker.WeightApp
import com.aidenir.weighttracker.data.WeightRepository
import com.aidenir.weighttracker.notifications.ReminderNotifier
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Polls Home Assistant every 15 minutes and, if the bed sensor is "off" and today has
 * no weigh-in yet, fires the reminder.
 *
 * The exact same reminder can also be pushed instantly by HA calling
 * [HomeAssistantReceiver]. The watcher is a resilient fallback for when the
 * device is not reachable from the LAN.
 */
class HomeAssistantWatcher(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as WeightApp
        val settings = app.container.settingsRepository.settings.first()
        val ha = settings.homeAssistant
        if (!ha.isConfigured()) return Result.success()

        val today = WeightRepository.today()
        if (app.container.weightRepository.forDate(today) != null) return Result.success()

        val state = HomeAssistantClient().state(ha)
        if (state.equals("off", ignoreCase = true) || state.equals("not_home", ignoreCase = true)) {
            ReminderNotifier.show(applicationContext)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "ha_watcher"

        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<HomeAssistantWatcher>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                req
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
