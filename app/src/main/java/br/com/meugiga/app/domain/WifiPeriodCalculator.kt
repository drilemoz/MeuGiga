package br.com.meugiga.app.domain

import br.com.meugiga.app.domain.model.UsagePeriod
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Filtros exclusivos da aba Wi-Fi. Não dependem do ciclo da operadora. */
enum class WifiPeriod { TODAY, YESTERDAY, SEVEN_DAYS, THIRTY_DAYS, CUSTOM }

class WifiPeriodCalculator(private val clock: Clock = Clock.systemDefaultZone()) {
    fun resolve(
        selection: WifiPeriod,
        nowMillis: Long = clock.millis(),
        zoneId: ZoneId = clock.zone,
        customDay: LocalDate? = null,
    ): UsagePeriod {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        val start: LocalDate
        val end: Long
        val label: String
        when (selection) {
            WifiPeriod.TODAY -> { start = today; end = nowMillis; label = "Hoje" }
            WifiPeriod.YESTERDAY -> {
                start = today.minusDays(1)
                end = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
                label = "Ontem"
            }
            WifiPeriod.SEVEN_DAYS -> { start = today.minusDays(6); end = nowMillis; label = "7 dias" }
            WifiPeriod.THIRTY_DAYS -> { start = today.minusDays(29); end = nowMillis; label = "30 dias" }
            WifiPeriod.CUSTOM -> {
                val day = requireNotNull(customDay) { "Selecione um dia" }
                require(!day.isAfter(today)) { "Data futura não permitida" }
                start = day
                end = minOf(nowMillis, day.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli())
                label = day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
        }
        val begin = start.atStartOfDay(zoneId).toInstant().toEpochMilli()
        return UsagePeriod(begin, end.coerceAtLeast(begin), label, "wifi:${selection.name}:$start")
    }
}
