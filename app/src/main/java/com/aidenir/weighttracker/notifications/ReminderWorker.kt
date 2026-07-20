package com.aidenir.weighttracker.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aidenir.weighttracker.WeightApp
import com.aidenir.weighttracker.motion.OutOfBedService
import kotlinx.coroutines.flow.first

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext.applicationContext as WeightApp
        val settings = app.container.settingsRepository.settings.first()
        val reminder = settings.reminder
        if (!reminder.enabled) return Result.success()

        try {
            val already = app.container.weightRepository.forDate(
                com.aidenir.weighttracker.data.WeightRepository.today()
            )
            if (already == null) {
                if (reminder.useMotion) {
                    OutOfBedService.start(applicationContext)
                } else {
                    ReminderNotifier.show(applicationContext)
                }
            }
            return Result.success()
        } finally {
            // Enqueueing under our own unique-work name with REPLACE cancels
            // the running instance — do it after the fire so we don't cut
            // our own coroutine short before show() runs.
            ReminderScheduler(applicationContext).reschedule(reminder)
        }
    }
}
