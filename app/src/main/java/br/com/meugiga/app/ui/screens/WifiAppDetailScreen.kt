package br.com.meugiga.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.meugiga.app.ui.components.AppIcon
import br.com.meugiga.app.utils.ByteFormatter
import br.com.meugiga.app.viewmodel.WifiUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Detalhamento somente dos dados Wi-Fi da consulta atual, sem gráficos da rede móvel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiAppDetailScreen(
    uid: Int,
    state: WifiUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val report = state.report
    val usage = report?.apps?.firstOrNull { it.uid == uid }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(usage?.descriptor?.label ?: "Detalhes Wi-Fi",
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (usage == null || report == null) {
                item { Text("Os dados deste aplicativo não estão mais disponíveis. Volte à aba Wi-Fi e atualize a consulta.") }
            } else {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(usage.descriptor.packageName, contentDescription = null, size = 56.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(usage.descriptor.label, style = MaterialTheme.typography.titleLarge)
                            Text(
                                usage.descriptor.packageName ?: "UID ${usage.uid} · Sistema ou aplicativo não identificado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                item {
                    WifiMetricCard(
                        title = "Total Wi-Fi · ${report.period.label}",
                        value = ByteFormatter.format(usage.totalBytes),
                        icon = Icons.Rounded.Wifi,
                        prominent = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WifiMetricCard("Download", ByteFormatter.format(usage.rxBytes), Icons.Rounded.ArrowDownward,
                            modifier = Modifier.weight(1f))
                        WifiMetricCard("Upload", ByteFormatter.format(usage.txBytes), Icons.Rounded.ArrowUpward,
                            modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Text(
                        "${(usage.shareOfTotal * 100).roundToInt()}% do total Wi-Fi informado pelo Android para o período.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "UID: ${usage.uid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (usage.descriptor.packageNames.size > 1) {
                        Text(
                            "Este UID é compartilhado por: ${usage.descriptor.packageNames.joinToString()}. " +
                                "O consumo não pode ser separado entre esses pacotes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    val captured = Instant.ofEpochMilli(report.capturedAtMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")))
                    Text(
                        "Leitura: $captured. Valores agregados por UID, sem histórico individual nem gráficos Wi-Fi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
