package br.com.meugiga.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WifiPeriodCalculatorTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val now = Instant.parse("2026-10-14T21:00:00Z")
    private val periods = WifiPeriodCalculator(Clock.fixed(now, zone))

    @Test fun `hoje comeca a meia noite local e termina agora`() {
        val result = periods.resolve(WifiPeriod.TODAY)
        assertEquals(LocalDate.of(2026, 10, 14).atStartOfDay(zone).toInstant().toEpochMilli(), result.startMillis)
        assertEquals(now.toEpochMilli(), result.endMillis)
        assertEquals("Hoje", result.label)
    }

    @Test fun `ontem usa intervalo completo sem incluir hoje`() {
        val result = periods.resolve(WifiPeriod.YESTERDAY)
        assertEquals(LocalDate.of(2026, 10, 13).atStartOfDay(zone).toInstant().toEpochMilli(), result.startMillis)
        assertEquals(LocalDate.of(2026, 10, 14).atStartOfDay(zone).toInstant().toEpochMilli(), result.endMillis)
    }

    @Test fun `sete dias incluem dia atual e os seis anteriores`() {
        val result = periods.resolve(WifiPeriod.SEVEN_DAYS)
        assertEquals(LocalDate.of(2026, 10, 8).atStartOfDay(zone).toInstant().toEpochMilli(), result.startMillis)
        assertEquals(now.toEpochMilli(), result.endMillis)
    }

    @Test fun `trinta dias incluem dia atual e vinte nove anteriores`() {
        val result = periods.resolve(WifiPeriod.THIRTY_DAYS)
        assertEquals(LocalDate.of(2026, 9, 15).atStartOfDay(zone).toInstant().toEpochMilli(), result.startMillis)
        assertTrue(result.cacheKey.startsWith("wifi:"))
        assertFalse(result.cacheKey.contains("mobile"))
    }
}
