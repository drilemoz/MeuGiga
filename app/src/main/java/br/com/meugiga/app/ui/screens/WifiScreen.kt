package br.com.meugiga.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.meugiga.app.domain.WifiPeriod
import br.com.meugiga.app.domain.WifiRankingPresentation
import br.com.meugiga.app.domain.WifiSortOrder
import br.com.meugiga.app.domain.model.AppUsage
import br.com.meugiga.app.ui.components.SingleDayPickerButton
import br.com.meugiga.app.ui.components.AppIcon
import br.com.meugiga.app.utils.ByteFormatter
import br.com.meugiga.app.viewmodel.WifiReadError
import br.com.meugiga.app.viewmodel.WifiUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

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
    onSelectDay: (java.time.LocalDate) -> Unit,
    onRefresh: () -> Unit,
    onGrantUsageAccess: () -> Unit,
    onOpenApp: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Consulta realizada apenas enquanto a aba está ativa.
    LaunchedEffect(Unit) { onOpen() }
    var query by rememberSaveable { mutableStateOf("") }
    var sortName by rememberSaveable { mutableStateOf(WifiSortOrder.TOTAL.name) }
    val sortOrder = WifiSortOrder.valueOf(sortName)
    val report = state.report
    val apps = remember(report?.apps, query, sortOrder) {
        WifiRankingPresentation.filterAndSort(report?.apps.orEmpty(), query, sortOrder)
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Consumo Wi-Fi", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Independente da franquia móvel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    enabled = !state.refreshing,
                ) {
                    if (state.refreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Atualizar consumo Wi-Fi", modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wifiPresets) { (period, label) ->
                        FilterChip(
                            selected = state.selectedPeriod == period,
                            onClick = { onSelectPeriod(period) },
                            label = { Text(label) },
                        )
                    }
                }
                SingleDayPickerButton(
                    selectedDay = state.customDay.takeIf { state.selectedPeriod == WifiPeriod.CUSTOM },
                    onDaySelected = onSelectDay,
                )
            }
            if (state.selectedPeriod == WifiPeriod.CUSTOM) {
                Text(
                    "Dia selecionado: ${state.customDay?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (state.refreshing) {
            item {
                Text(
                    "Consultando estatísticas do Android…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.error?.let { error ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            when (error) {
                                WifiReadError.PERMISSION -> "O acesso às estatísticas está desativado."
                                WifiReadError.UNAVAILABLE -> "O Android não disponibilizou o consumo Wi-Fi deste período."
                                WifiReadError.UNEXPECTED -> "Não foi possível consultar o Wi-Fi: ${state.errorMessage ?: "erro desconhecido"}."
                            },
                        )
                        if (error == WifiReadError.PERMISSION) {
                            Button(onClick = onGrantUsageAccess) { Text("Autorizar acesso ao uso") }
                        }
                    }
                }
            }
        }
        if (report != null) {
            item {
                WifiMetricCard(
                    title = "Total consumido",
                    value = ByteFormatter.format(report.totalBytes),
                    icon = Icons.Rounded.Wifi,
                    modifier = Modifier.fillMaxWidth(),
                    prominent = true,
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WifiMetricCard(
                        title = "Download",
                        value = ByteFormatter.format(report.rxBytes),
                        icon = Icons.Rounded.ArrowDownward,
                        modifier = Modifier.weight(1f),
                    )
                    WifiMetricCard(
                        title = "Upload",
                        value = ByteFormatter.format(report.txBytes),
                        icon = Icons.Rounded.ArrowUpward,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                val captured = remember(report.capturedAtMillis) {
                    Instant.ofEpochMilli(report.capturedAtMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.forLanguageTag("pt-BR")))
                }
                Text(
                    "Última leitura: $captured · Atualize no botão acima",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Text("Aplicativos no Wi-Fi", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Toque em um aplicativo para consultar download, upload e participação no período.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (report.appsAvailable && report.apps.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Buscar aplicativo") },
                        placeholder = { Text("Nome, pacote ou UID") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = if (query.isNotEmpty()) {{
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Limpar busca")
                            }
                        }} else null,
                    )
                }
                item {
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${apps.size} de ${report.apps.size} aplicativos",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Icon(Icons.Rounded.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(sortOrder.label)
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                WifiSortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.label) },
                                        onClick = { sortName = order.name; expanded = false },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            when {
                !report.appsAvailable -> item {
                    Text("O Android informou o total, mas não liberou o consumo por aplicativo.")
                }
                report.apps.isEmpty() -> item { Text("Nenhum consumo por aplicativo neste período.") }
                apps.isEmpty() -> item {
                    Text("Nenhum aplicativo corresponde à busca “${query.trim()}”.")
                }
                else -> items(apps, key = { it.uid }) { app ->
                    WifiAppUsageRow(app, onClick = { onOpenApp(app.uid) })
                }
            }
            if (report.appsAvailable && report.apps.isNotEmpty()) {
                item {
                    val unassigned = WifiRankingPresentation.notAttributedBytes(
                        report.totalBytes, report.apps, report.appsAvailable,
                    )
                    Text(
                        when {
                            unassigned == null -> "A soma por aplicativo difere do total do aparelho. " +
                                "O Android pode agregar ou contabilizar tráfego de forma diferente por UID."
                            unassigned > 0L -> "Diferença entre o total e a soma dos aplicativos: " +
                                "${ByteFormatter.format(unassigned)} (sistema, outros ou dados não atribuídos)."
                            else -> "A soma dos aplicativos corresponde ao total informado pelo Android."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Text(
                    "Inclui possíveis transferências locais. Não mede velocidade em Mbps " +
                        "nem separa roteadores. Sem gráficos ou histórico Wi-Fi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun WifiMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.size(30.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Text(
                value,
                style = if (prominent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Barra sem indicador circular no final; exclusiva do ranking Wi-Fi. */
@Composable
private fun WifiAppUsageRow(app: AppUsage, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon(app.descriptor.packageName, contentDescription = null, size = 44.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        app.descriptor.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(ByteFormatter.format(app.totalBytes), fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall)
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                        .clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    val fraction = app.shareOfTotal.coerceIn(0.0, 1.0).toFloat()
                    if (fraction > 0f) {
                        Box(
                            Modifier.fillMaxWidth(fraction).height(6.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)),
                        )
                    }
                }
                Text(
                    "${(app.shareOfTotal * 100).roundToInt()}% do total · Toque para detalhes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
