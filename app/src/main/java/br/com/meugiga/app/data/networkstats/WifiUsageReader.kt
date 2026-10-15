package br.com.meugiga.app.data.networkstats

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import br.com.meugiga.app.domain.model.UidUsage
import br.com.meugiga.app.domain.model.UsagePeriod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Consulta independente, em memória, sem gravar bytes Wi-Fi no banco móvel. */
data class WifiStatsRead(
    val period: UsagePeriod,
    val deviceRxBytes: Long,
    val deviceTxBytes: Long,
    val uidUsage: List<UidUsage>,
    val appsAvailable: Boolean,
    val capturedAtMillis: Long,
)

interface WifiUsageReader {
    suspend fun read(period: UsagePeriod): WifiStatsRead
}

@Suppress("DEPRECATION") // NetworkStatsManager usa TYPE_WIFI nos overloads públicos desde API 23.
class AndroidWifiUsageReader(
    context: Context,
    private val accessController: UsageAccessController,
    private val manager: NetworkStatsManager = context.getSystemService(NetworkStatsManager::class.java),
) : WifiUsageReader {
    override suspend fun read(period: UsagePeriod): WifiStatsRead = withContext(Dispatchers.IO) {
        if (!accessController.hasAccess()) throw UsageAccessDeniedException()
        try {
            val device = manager.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI, null, period.startMillis, period.endMillis,
            ) ?: throw NetworkStatisticsUnavailableException()

            // Alguns fabricantes permitem o total do dispositivo, mas bloqueiam a lista por UID.
            // Não substituir a lista indisponível por dados móveis ou por valores inventados.
            val perApp = try {
                val stats = manager.querySummary(
                    ConnectivityManager.TYPE_WIFI, null, period.startMillis, period.endMillis,
                )
                if (stats == null) null else stats.use { cursor ->
                    val totals = mutableMapOf<Int, LongArray>()
                    val bucket = NetworkStats.Bucket()
                    while (cursor.hasNextBucket()) {
                        cursor.getNextBucket(bucket)
                        val values = totals.getOrPut(bucket.uid) { longArrayOf(0L, 0L) }
                        values[0] += bucket.rxBytes.coerceAtLeast(0L)
                        values[1] += bucket.txBytes.coerceAtLeast(0L)
                    }
                    totals.map { (uid, bytes) -> UidUsage(uid, bytes[0], bytes[1]) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: SecurityException) {
                null
            } catch (_: Exception) {
                null
            }
            WifiStatsRead(
                period = period,
                deviceRxBytes = device.rxBytes.coerceAtLeast(0L),
                deviceTxBytes = device.txBytes.coerceAtLeast(0L),
                uidUsage = perApp.orEmpty(),
                appsAvailable = perApp != null,
                capturedAtMillis = System.currentTimeMillis(),
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (denied: UsageAccessDeniedException) {
            throw denied
        } catch (unavailable: NetworkStatisticsUnavailableException) {
            throw unavailable
        } catch (denied: SecurityException) {
            throw UsageAccessDeniedException(denied)
        } catch (error: Exception) {
            throw NetworkStatisticsUnavailableException(error)
        }
    }
}
