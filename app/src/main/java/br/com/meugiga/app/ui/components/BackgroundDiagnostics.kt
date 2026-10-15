package br.com.meugiga.app.ui.components

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.work.WorkInfo
import androidx.work.WorkManager
import br.com.meugiga.app.worker.UsageWorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BackgroundDiagnostics(intervalMinutes: Int, hasUsageAccess: Boolean) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var workLabel by remember { mutableStateOf("Consultando agendamento…") }
    var batteryExempt by remember { mutableStateOf(false) }
    var lastSuccess by remember { mutableStateOf(0L) }
    LaunchedEffect(intervalMinutes, hasUsageAccess, refreshKey) {
        val info = withContext(Dispatchers.IO) {
            val works = runCatching {
                WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWork(UsageWorkScheduler.UNIQUE_WORK_NAME)
                    .get()
            }.getOrNull()
            val prefs = context.getSharedPreferences("meugiga_worker_status", 0)
            Triple(works, prefs.getLong("last_success_millis", 0L),
                (context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager)
                    .isIgnoringBatteryOptimizations(context.packageName))
        }
        lastSuccess = info.second
        batteryExempt = info.third
        // Capture a referência anulável em uma variável local estável. `info.first` é
        // uma propriedade pública de Triple, não elegível para smart cast em Kotlin.
        val scheduledWorks = info.first
        workLabel = when {
            intervalMinutes == 0 -> "Desativado pelo usuário"
            !hasUsageAccess -> "Sem acesso às estatísticas"
            scheduledWorks == null -> "Estado do agendamento indisponível"
            scheduledWorks.isEmpty() -> "Não encontrado: abra a tela novamente após autorizar o acesso"
            scheduledWorks.any { it.state == WorkInfo.State.RUNNING } -> "Consulta em execução"
            scheduledWorks.any { it.state == WorkInfo.State.ENQUEUED } -> "Agendado (horário controlado pelo Android)"
            scheduledWorks.any { it.state == WorkInfo.State.BLOCKED } -> "Aguardando condições do sistema"
            else -> "Sem trabalho ativo: ${scheduledWorks.first().state}"
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Status: $workLabel", style = MaterialTheme.typography.bodyMedium)
        Text(
            if (batteryExempt) "O Android informa que a otimização de bateria não restringe este app."
            else "O Android pode restringir a atividade em segundo plano deste app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            if (lastSuccess > 0) "Última consulta automática concluída: " +
                Instant.ofEpochMilli(lastSuccess).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
            else "Nenhuma consulta automática concluída registrada nesta instalação.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedButton(onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}"))
            runCatching { context.startActivity(intent) }
        }, modifier = Modifier.fillMaxWidth()) { Text("Abrir informações do aplicativo (Bateria)") }
        OutlinedButton(onClick = {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            runCatching { context.startActivity(intent) }
        }, modifier = Modifier.fillMaxWidth()) { Text("Ver opções de otimização de bateria") }
        OutlinedButton(onClick = { refreshKey++ }) { Text("Verificar status") }
        Text(
            "WorkManager não impede que o Android encerre o processo. As consultas podem " +
                "atrasar por Doze, restrições do fabricante ou bateria baixa. " +
                "No Xiaomi/Redmi, confira Bateria e Inicialização automática, se disponíveis.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
