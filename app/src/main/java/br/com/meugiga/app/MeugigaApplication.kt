package br.com.meugiga.app

import android.app.Application
import br.com.meugiga.app.domain.BackgroundSyncPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MeugigaApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notificationManager.createChannels()
        applicationScope.launch {
            container.settingsRepository.settings
                .map { it.onboardingComplete to it.backgroundIntervalMinutes }
                .distinctUntilChanged()
                .collect { (completed, minutes) ->
                    val hasAccess = container.usageAccessController.hasAccess()
                    container.workScheduler.configure(
                        intervalMinutes = minutes,
                        enabled = BackgroundSyncPolicy.shouldSchedule(completed, hasAccess, minutes),
                    )
                }
        }
    }
}

