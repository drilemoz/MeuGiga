package br.com.meugiga.app.domain

import br.com.meugiga.app.domain.model.AppUsage
import java.util.Locale

/** Somente ordenação e pesquisa do ranking Wi-Fi, sem consultas ou gravações. */
enum class WifiSortOrder(val label: String) {
    TOTAL("Total"),
    DOWNLOAD("Download"),
    UPLOAD("Upload"),
    NAME("Nome"),
}

object WifiRankingPresentation {
    fun filterAndSort(
        apps: List<AppUsage>,
        query: String,
        order: WifiSortOrder,
    ): List<AppUsage> {
        val search = query.trim().lowercase(Locale.ROOT)
        val filtered = if (search.isEmpty()) apps else apps.filter { app ->
            val descriptor = app.descriptor
            descriptor.label.lowercase(Locale.ROOT).contains(search) ||
                descriptor.packageName?.lowercase(Locale.ROOT)?.contains(search) == true ||
                descriptor.packageNames.any { it.lowercase(Locale.ROOT).contains(search) } ||
                descriptor.uid.toString().contains(search)
        }
        val comparator = when (order) {
            WifiSortOrder.TOTAL -> compareByDescending<AppUsage> { it.totalBytes }
            WifiSortOrder.DOWNLOAD -> compareByDescending<AppUsage> { it.rxBytes }
            WifiSortOrder.UPLOAD -> compareByDescending<AppUsage> { it.txBytes }
            WifiSortOrder.NAME -> compareBy<AppUsage> { it.descriptor.label.lowercase(Locale.ROOT) }
        }
        return filtered.sortedWith(comparator.thenBy { it.uid })
    }

    /** A diferença só é calculada quando os bytes por UID não excedem os do dispositivo. */
    fun notAttributedBytes(deviceTotal: Long, apps: List<AppUsage>, appsAvailable: Boolean): Long? {
        if (!appsAvailable || deviceTotal < 0L) return null
        var appsTotal = 0L
        for (app in apps) {
            if (app.totalBytes < 0L || app.totalBytes > deviceTotal - appsTotal) return null
            appsTotal += app.totalBytes
        }
        return deviceTotal - appsTotal
    }
}
