package br.com.meugiga.app.domain

import br.com.meugiga.app.domain.model.AppDescriptor
import br.com.meugiga.app.domain.model.AppUsage
import br.com.meugiga.app.domain.model.UidUsage

/** O ranking usa os totais por UID; o total do dispositivo não é somado ao ranking. */
object WifiUsageCalculator {
    fun appRanking(
        entries: List<UidUsage>,
        descriptors: Map<Int, AppDescriptor>,
        deviceTotalBytes: Long,
    ): List<AppUsage> = entries.asSequence()
        .filter { it.uid != SpecialUidClassifier.UID_ALL && it.totalBytes > 0L }
        .groupBy { it.uid }
        .map { (uid, values) ->
            val rx = values.sumOf { it.rxBytes.coerceAtLeast(0L) }
            val tx = values.sumOf { it.txBytes.coerceAtLeast(0L) }
            AppUsage(
                descriptor = descriptors[uid] ?: AppDescriptor(uid, null, "UID $uid"),
                rxBytes = rx,
                txBytes = tx,
                shareOfTotal = if (deviceTotalBytes > 0L) {
                    ((rx.toDouble() + tx.toDouble()) / deviceTotalBytes).coerceIn(0.0, 1.0)
                } else 0.0,
            )
        }
        .sortedWith(compareByDescending<AppUsage> { it.totalBytes }.thenBy { it.uid })
        .toList()
}
