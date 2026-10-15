package br.com.meugiga.app.domain

import br.com.meugiga.app.domain.model.TimelineUsage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Keeps cross-midnight buckets together; their within-bucket distribution is unknown. */
data class DailyUsageGroup(
    val label: String,
    val rxBytes: Long,
    val txBytes: Long,
)

object DailyUsageGrouper {
    private data class DaySpan(val start: LocalDate, val end: LocalDate)

    fun group(timeline: List<TimelineUsage>, zoneId: ZoneId): List<DailyUsageGroup> =
        timeline
            .filter { it.endMillis > it.startMillis }
            .groupBy {
                DaySpan(
                    Instant.ofEpochMilli(it.startMillis).atZone(zoneId).toLocalDate(),
                    Instant.ofEpochMilli(it.endMillis - 1).atZone(zoneId).toLocalDate(),
                )
            }
            .toSortedMap(compareBy<DaySpan>({ it.start }, { it.end }))
            .map { (span, values) ->
                val start = span.start.formatDay()
                val end = span.end.formatDay()
                DailyUsageGroup(
                    label = if (span.start == span.end) start else "$start–$end",
                    rxBytes = values.sumOf { it.rxBytes },
                    txBytes = values.sumOf { it.txBytes },
                )
            }

    private fun LocalDate.formatDay() =
        "${dayOfMonth.toString().padStart(2, '0')}/${monthValue.toString().padStart(2, '0')}"
}
