package com.aidenir.weighttracker

import android.app.Application
import androidx.work.Configuration
import com.aidenir.weighttracker.homeassistant.HomeAssistantWatcher
import com.aidenir.weighttracker.notifications.NotificationChannels
import com.aidenir.weighttracker.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeightApp : Application(), Configuration.Provider {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationChannels.ensureRegistered(this)

        CoroutineScope(Dispatchers.Default).launch {
            val settings = container.settingsRepository.settings.first()
            ReminderScheduler(this@WeightApp).reschedule(settings.reminder)
            HomeAssistantWatcher.schedule(this@WeightApp)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
