package br.com.meugiga.app.data.repository

import br.com.meugiga.app.data.networkstats.WifiUsageReader
import br.com.meugiga.app.domain.WifiUsageCalculator
import br.com.meugiga.app.domain.model.AppUsage
import br.com.meugiga.app.domain.model.UsagePeriod
import kotlinx.coroutines.CancellationException

/** Sem Room, WorkManager, franquia, alertas ou gráficos da rede móvel. */
data class WifiUsageReport(
    val period: UsagePeriod,
    val rxBytes: Long,
    val txBytes: Long,
    val apps: List<AppUsage>,
    val appsAvailable: Boolean,
    val capturedAtMillis: Long,
) {
    val totalBytes: Long get() = rxBytes + txBytes
}

class WifiUsageRepository(
    private val reader: WifiUsageReader,
    private val packageCatalog: PackageCatalog,
) {
    suspend fun load(period: UsagePeriod): WifiUsageReport {
        val read = reader.read(period)
        val labels = try {
            if (read.uidUsage.isNotEmpty()) {
                packageCatalog.resolve(read.uidUsage.map { it.uid })
            } else emptyMap()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Problemas no catálogo local não devem esconder os bytes válidos do dispositivo.
            emptyMap()
        }
        return WifiUsageReport(
            period = period,
            rxBytes = read.deviceRxBytes,
            txBytes = read.deviceTxBytes,
            apps = WifiUsageCalculator.appRanking(
                read.uidUsage, labels, read.deviceRxBytes + read.deviceTxBytes,
            ),
            appsAvailable = read.appsAvailable,
            capturedAtMillis = read.capturedAtMillis,
        )
    }
}
