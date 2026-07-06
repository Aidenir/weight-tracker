package com.aidenir.weighttracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

object NotificationChannels {
    const val REMINDER = "weight_reminder"
    const val SERVICE = "weight_service"

    fun ensureRegistered(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(REMINDER) == null) {
            manager.createNotificationChannel(
                NotificationChannel(REMINDER, "Weigh-in reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Daily reminder to log your weight."
                    setShowBadge(true)
                }
            )
        }
        if (manager.getNotificationChannel(SERVICE) == null) {
            manager.createNotificationChannel(
                NotificationChannel(SERVICE, "Out-of-bed detection", NotificationManager.IMPORTANCE_MIN).apply {
                    description = "Runs in the background to detect when you get up."
                }
            )
        }
    }
}
