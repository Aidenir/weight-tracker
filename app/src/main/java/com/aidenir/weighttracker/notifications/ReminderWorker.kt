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

        // Reschedule for tomorrow first — always keep the chain going.
        ReminderScheduler(applicationContext).reschedule(reminder)

        val already = app.container.weightRepository.forDate(
            com.aidenir.weighttracker.data.WeightRepository.today()
        )
        if (already != null) return Result.success()

        if (reminder.useMotion) {
            OutOfBedService.start(applicationContext)
        } else {
            ReminderNotifier.show(applicationContext)
        }
        return Result.success()
    }
}
