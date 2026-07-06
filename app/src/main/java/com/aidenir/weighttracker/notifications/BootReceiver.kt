package com.aidenir.weighttracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aidenir.weighttracker.WeightApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return
        val pending = goAsync()
        val app = context.applicationContext as WeightApp
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = app.container.settingsRepository.settings.first()
                ReminderScheduler(context).reschedule(settings.reminder)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
    }
}
