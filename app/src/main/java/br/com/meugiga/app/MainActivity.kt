package br.com.meugiga.app

import android.os.Bundle
import android.provider.Settings
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import br.com.meugiga.app.export.UsageCsvExporter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.meugiga.app.ui.MeugigaApp
import br.com.meugiga.app.ui.screens.OnboardingScreen
import br.com.meugiga.app.ui.screens.WhatsNewScreen
import br.com.meugiga.app.ui.theme.MeugigaTheme
import br.com.meugiga.app.viewmodel.MainViewModel
import br.com.meugiga.app.viewmodel.WifiViewModel

class MainActivity : ComponentActivity() {
    private val createCsvDocument = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri: Uri? ->
        val report = mainViewModel.consumePreparedCsvExport()
        if (uri != null) {
            if (report == null) {
                Toast.makeText(this, "Relatório indisponível. Tente exportar novamente.", Toast.LENGTH_LONG).show()
            } else {
                lifecycleScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            val data = UsageCsvExporter.bytes(report)
                            contentResolver.openOutputStream(uri)?.use { output ->
                                output.write(data)
                                output.flush()
                            } ?: error("Não foi possível abrir o arquivo escolhido.")
                        }
                    }
                    Toast.makeText(
                        this@MainActivity,
                        if (result.isSuccess) "CSV salvo com sucesso." else "Falha ao salvar CSV: ${result.exceptionOrNull()?.message ?: "erro desconhecido"}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private val updatedInstall: Boolean by lazy(::detectUpdatedInstall)

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.factory((application as MeugigaApplication).container)
    }

    private val wifiViewModel: WifiViewModel by viewModels {
        WifiViewModel.factory((application as MeugigaApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by mainViewModel.state.collectAsStateWithLifecycle()
            val wifiState by wifiViewModel.state.collectAsStateWithLifecycle()
            MeugigaTheme(state.settings.themeMode) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when {
                        !state.initialized -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        !state.settings.onboardingComplete -> OnboardingScreen(
                            hasUsageAccess = state.hasUsageAccess,
                            onSavePlan = mainViewModel::savePlan,
                            onOpenUsageAccess = ::openUsageAccessSettings,
                            onFinish = mainViewModel::finishOnboarding,
                        )
                        updatedInstall &&
                            state.settings.lastSeenWhatsNewVersionCode < BuildConfig.VERSION_CODE ->
                            WhatsNewScreen(
                                onContinue = {
                                    mainViewModel.markWhatsNewSeen(BuildConfig.VERSION_CODE)
                                },
                            )
                        else -> MeugigaApp(
                            state = state,
                            wifiState = wifiState,
                            onWifiOpen = wifiViewModel::onOpened,
                            onWifiRefresh = wifiViewModel::refresh,
                            onWifiPeriodSelected = wifiViewModel::selectPeriod,
                            onGrantUsageAccess = ::openUsageAccessSettings,
                            onRefresh = mainViewModel::refreshNow,
                            onPresetSelected = mainViewModel::selectPeriod,
                            onCustomSelected = mainViewModel::selectCustomPeriod,
                            onSavePlan = mainViewModel::savePlan,
                            onSetPersistentNotification = mainViewModel::setPersistentNotification,
                            onSetAlerts = mainViewModel::setAlerts,
                            onSetBackgroundInterval = mainViewModel::setBackgroundInterval,
                            onSetTheme = mainViewModel::setTheme,
                            onExportCsv = ::exportCsvReport,
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.refreshPermissionState()
        mainViewModel.startForegroundUpdates()
    }

    override fun onStop() {
        mainViewModel.stopForegroundUpdates()
        super.onStop()
    }

    @Suppress("DEPRECATION")
    private fun detectUpdatedInstall(): Boolean = runCatching {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.lastUpdateTime > packageInfo.firstInstallTime
    }.getOrDefault(false)

    private fun exportCsvReport() {
        if (!mainViewModel.prepareCsvExport()) {
            Toast.makeText(this, "Atualize o período antes de exportar o relatório.", Toast.LENGTH_LONG).show()
            return
        }
        val suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        createCsvDocument.launch("MeuGiga-consumo-$suffix.csv")
    }

    private fun openUsageAccessSettings() {
        runCatching { startActivity(mainViewModel.usageAccessSettingsIntent()) }
            .onFailure { startActivity(android.content.Intent(Settings.ACTION_SETTINGS)) }
    }
}
