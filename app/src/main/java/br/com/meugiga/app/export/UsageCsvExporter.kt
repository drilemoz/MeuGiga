package br.com.meugiga.app.export

import br.com.meugiga.app.domain.model.AppUsage
import br.com.meugiga.app.domain.model.ReportSource
import br.com.meugiga.app.domain.model.UsageReport
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Relatório CSV local, UTF-8 com BOM para Excel e delimitador ponto e vírgula.
 * Os campos textuais controlados por apps externos são escapados contra CSV/formula injection.
 * A exportação inclui o período completo do relatório, não a pesquisa visual aplicada na tela.
 */
object UsageCsvExporter {
    private val header = listOf(
        "tipo", "periodo", "inicio", "fim_exclusivo", "capturado_em", "origem",
        "uid", "aplicativo", "pacote", "download_bytes", "upload_bytes",
        "total_bytes", "percentual_do_total",
    )

    fun bytes(report: UsageReport, zone: ZoneId = ZoneId.systemDefault()): ByteArray {
        require(report.source != ReportSource.EMPTY && report.capturedAtMillis > 0L) {
            "Não existe relatório válido para exportar."
        }
        val common = listOf(
            textCell(report.period.label),
            textCell(formatDate(report.period.startMillis, zone)),
            textCell(formatDate(report.period.endMillis, zone)),
            textCell(formatDate(report.capturedAtMillis, zone)),
            textCell(if (report.source == ReportSource.CACHE) "CACHE" else "AO_VIVO"),
        )
        val content = buildString {
            append(header.joinToString(";")).append("\r\n")
            report.apps.sortedWith(compareByDescending<AppUsage> { it.totalBytes }.thenBy { it.uid })
                .forEach { app ->
                    val fields = listOf(
                        textCell("APLICATIVO"),
                    ) + common + listOf(
                        app.uid.toString(),
                        textCell(app.descriptor.label, untrusted = true),
                        textCell(app.descriptor.packageName.orEmpty(), untrusted = true),
                        app.rxBytes.toString(),
                        app.txBytes.toString(),
                        app.totalBytes.toString(),
                        decimalPercent(app.shareOfTotal * 100.0),
                    )
                    append(fields.joinToString(";")).append("\r\n")
                }
            val summary = listOf(textCell("TOTAL")) + common + listOf(
                "", textCell("Total do aparelho"), "",
                report.totalRxBytes.toString(),
                report.totalTxBytes.toString(),
                report.totalBytes.toString(),
                decimalPercent(if (report.totalBytes > 0L) 100.0 else 0.0),
            )
            append(summary.joinToString(";")).append("\r\n")
        }
        return byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + content.toByteArray(Charsets.UTF_8)
    }

    private fun formatDate(epochMillis: Long, zone: ZoneId): String =
        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    private fun decimalPercent(percent: Double): String =
        String.format(Locale.forLanguageTag("pt-BR"), "%.2f", percent)

    private fun textCell(input: String, untrusted: Boolean = false): String {
        val cleaned = input.replace("\u0000", "")
        val significant = cleaned.trimStart { it.isWhitespace() || it == '\uFEFF' }
        val dangerous = significant.firstOrNull() in listOf('=', '+', '-', '@')
        val safe = if (untrusted && dangerous) "'$cleaned" else cleaned
        return "\"${safe.replace("\"", "\"\"")}\""
    }
}
