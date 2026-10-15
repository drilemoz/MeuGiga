package br.com.meugiga.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.meugiga.app.domain.WifiPeriod
import br.com.meugiga.app.ui.components.AppUsageRow
import br.com.meugiga.app.ui.components.CompactMetric
import br.com.meugiga.app.ui.components.CompactMetricsGrid
import br.com.meugiga.app.utils.ByteFormatter
import br.com.meugiga.app.viewmodel.WifiReadError
import br.com.meugiga.app.viewmodel.WifiUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val wifiPresets = listOf(
    WifiPeriod.TODAY to "Hoje",
    WifiPeriod.YESTERDAY to "Ontem",
    WifiPeriod.SEVEN_DAYS to "7 dias",
    WifiPeriod.THIRTY_DAYS to "30 dias",
)

@Composable
fun WifiScreen(
    state: WifiUiState,
    onOpen: () -> Unit,
    onSelectPeriod: (WifiPeriod) -> Unit,
    onRefresh: () -> Unit,
    onGrantUsageAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // NavHost ativa a consulta somente quando esta rota está visível.
    LaunchedEffect(Unit) { onOpen() }
    val report = state.report
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Consumo Wi-Fi", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Consulta independente. Não altera sua franquia nem os gráficos de dados móveis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(wifiPresets) { (period, label) ->
                    FilterChip(
                        selected = state.selectedPeriod == period,
                        onClick = { onSelectPeriod(period) },
                        label = { Text(label) },
                    )
                }
            }
        }
        item {
            Button(onClick = onRefresh, enabled = !state.refreshing) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Text(" Atualizar Wi-Fi")
            }
        }
        if (state.refreshing) {
            item {
                CircularProgressIndicator()
                Text("Consultando as estatísticas Wi-Fi do Android…")
            }
        }
        state.error?.let { error ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
                        Text(
                            when (error) {
                                WifiReadError.PERMISSION -> "O acesso às estatísticas está desativado."
                                WifiReadError.UNAVAILABLE -> "O Android não disponibilizou o consumo Wi-Fi deste período."
                                WifiReadError.UNEXPECTED -> "Não foi possível consultar o Wi-Fi: ${state.errorMessage ?: "erro desconhecido"}."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (error == WifiReadError.PERMISSION) {
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onGrantUsageAccess) { Text("Autorizar acesso ao uso") }
                        }
                    }
                }
            }
        }
        if (report != null) {
            item {
                CompactMetricsGrid(
                    metrics = listOf(
                        CompactMetric("Total Wi-Fi", ByteFormatter.format(report.totalBytes), Icons.Rounded.DataUsage),
                        CompactMetric("Download", ByteFormatter.format(report.rxBytes), Icons.Rounded.CloudDownload),
                        CompactMetric("Upload", ByteFormatter.format(report.txBytes), Icons.Rounded.CloudUpload),
                    ),
                    columns = 2,
                )
            }
            item {
                val captured = Instant.ofEpochMilli(report.capturedAtMillis)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.forLanguageTag("pt-BR")))
                AssistChip(onClick = onRefresh, label = { Text("Leitura: $captured · Atualizar") },
                    leadingIcon = { Icon(Icons.Rounded.Wifi, contentDescription = null) })
            }
            item {
                Text("Aplicativos no Wi-Fi", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Ranking independente, por UID. O total do aparelho pode diferir da soma dos aplicativos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                !report.appsAvailable -> item {
                    Text("O Android permitiu ler o total, mas não informou o consumo por aplicativo neste aparelho.")
                }
                report.apps.isEmpty() -> item { Text("Nenhum consumo por aplicativo encontrado neste período.") }
                else -> items(report.apps, key = { it.uid }) { usage ->
                    AppUsageRow(usage = usage, onClick = null)
                }
            }
            item {
                Text(
                    "Inclui tráfego via Wi-Fi, que pode abranger transferências na rede local. " +
                        "Não é um teste de velocidade em Mbps nem separa roteadores. " +
                        "Os valores dependem das estatísticas disponíveis no Android.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
