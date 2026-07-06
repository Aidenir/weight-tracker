package com.aidenir.weighttracker.homeassistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aidenir.weighttracker.WeightApp
import com.aidenir.weighttracker.data.WeightRepository
import com.aidenir.weighttracker.notifications.ReminderNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives a local intent (broadcast from a Home Assistant automation, Tasker,
 * or a shortcut) that says "the user got out of bed" and fires the reminder.
 *
 * Trigger from HA with the Android companion app or Tasker:
 *   adb shell am broadcast -a com.aidenir.weighttracker.action.OUT_OF_BED \
 *     -p com.aidenir.weighttracker
 */
class HomeAssistantReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_OUT_OF_BED) return
        val pending = goAsync()
        val app = context.applicationContext as WeightApp
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val already = app.container.weightRepository.forDate(WeightRepository.today())
                if (already == null) {
                    ReminderNotifier.show(context)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_OUT_OF_BED = "com.aidenir.weighttracker.action.OUT_OF_BED"
    }
}
