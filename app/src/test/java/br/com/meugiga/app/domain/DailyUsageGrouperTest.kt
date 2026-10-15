package br.com.meugiga.app.domain

import br.com.meugiga.app.domain.model.TimelineUsage
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyUsageGrouperTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private fun time(iso: String) = Instant.parse(iso).toEpochMilli()

    @Test
    fun `cross midnight bucket is never assigned wholly to start day`() {
        val points = DailyUsageGrouper.group(
            listOf(
                TimelineUsage(time("2026-10-15T02:30:00Z"), time("2026-10-15T03:30:00Z"), 400, 50),
                TimelineUsage(time("2026-10-15T04:00:00Z"), time("2026-10-15T05:00:00Z"), 100, 10),
            ), zone,
        )
        assertEquals(2, points.size)
        assertEquals("14/10–15/10", points[0].label)
        assertEquals(450L, points[0].rxBytes + points[0].txBytes)
        assertEquals("15/10", points[1].label)
        assertEquals(110L, points[1].rxBytes + points[1].txBytes)
    }

    @Test
    fun `bucket ending at midnight belongs to preceding day`() {
        val grouped = DailyUsageGrouper.group(
            listOf(TimelineUsage(time("2026-10-15T02:00:00Z"), time("2026-10-15T03:00:00Z"), 5, 2)), zone,
        )
        assertEquals("14/10", grouped.single().label)
    }

    @Test
    fun `totals remain intact while day buckets are combined`() {
        val inputs = listOf(
            TimelineUsage(time("2026-10-14T13:00:00Z"), time("2026-10-14T15:00:00Z"), 500, 60),
            TimelineUsage(time("2026-10-14T16:00:00Z"), time("2026-10-14T17:00:00Z"), 300, 20),
        )
        val grouped = DailyUsageGrouper.group(inputs, zone)
        assertEquals(1, grouped.size)
        assertEquals("14/10", grouped.single().label)
        assertEquals(inputs.sumOf { it.rxBytes }, grouped.sumOf { it.rxBytes })
        assertEquals(inputs.sumOf { it.txBytes }, grouped.sumOf { it.txBytes })
    }
}
