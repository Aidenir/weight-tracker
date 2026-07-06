package com.aidenir.weighttracker.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aidenir.weighttracker.data.ReminderConfig
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    private val wm = WorkManager.getInstance(context)

    fun reschedule(config: ReminderConfig) {
        wm.cancelUniqueWork(WORK_NAME)
        if (!config.enabled) return
        val delayMs = millisUntilNext(config.hour, config.minute)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private fun millisUntilNext(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!next.after(now)) next.add(Calendar.DAY_OF_MONTH, 1)
        return next.timeInMillis - now.timeInMillis
    }

    companion object {
        const val WORK_NAME = "weight_reminder"
    }
}
