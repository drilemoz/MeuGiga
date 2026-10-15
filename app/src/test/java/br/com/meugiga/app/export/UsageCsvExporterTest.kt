package br.com.meugiga.app.export

import br.com.meugiga.app.domain.model.AppDescriptor
import br.com.meugiga.app.domain.model.AppUsage
import br.com.meugiga.app.domain.model.ReportSource
import br.com.meugiga.app.domain.model.UsagePeriod
import br.com.meugiga.app.domain.model.UsageReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset

class UsageCsvExporterTest {
    private fun sample(
        source: ReportSource = ReportSource.LIVE,
        apps: List<AppUsage> = listOf(
            AppUsage(AppDescriptor(200, "com.test", "Aplicativo Árvore"), 500L, 100L, 0.60),
            AppUsage(AppDescriptor(100, "com.outro", "Outro app"), 350L, 50L, 0.40),
        ),
    ): UsageReport = UsageReport(
        period = UsagePeriod(0L, 3_600_000L, "Hoje", "today"),
        totalRxBytes = 850L,
        totalTxBytes = 150L,
        apps = apps,
        timeline = emptyList(),
        appTimelines = emptyMap(),
        capturedAtMillis = 3_600_000L,
        source = source,
    )

    private fun csv(report: UsageReport) =
        String(UsageCsvExporter.bytes(report, ZoneOffset.UTC).drop(3).toByteArray(), Charsets.UTF_8)

    @Test fun `emite BOM UTF8 e cabecalho com treze campos consistentes`() {
        val bytes = UsageCsvExporter.bytes(sample(), ZoneOffset.UTC)
        assertEquals(0xEF, bytes[0].toInt() and 0xff)
        assertEquals(0xBB, bytes[1].toInt() and 0xff)
        assertEquals(0xBF, bytes[2].toInt() and 0xff)
        val lines = csv(sample()).trimEnd().split("\r\n")
        assertEquals(4, lines.size)
        assertEquals(13, lines[0].split(';').size)
        assertTrue(lines[1].startsWith("\"APLICATIVO\";\"Hoje\""))
        assertTrue(lines[3].contains("\"Total do aparelho\""))
        assertTrue(lines[3].endsWith("850;150;1000;100,00"))
    }

    @Test fun `mantem uid e inteiros em bytes na ordem de consumo`() {
        val lines = csv(sample()).split("\r\n")
        assertTrue(lines[1].contains(";200;\"Aplicativo Árvore\";\"com.test\";500;100;600;60,00"))
        assertTrue(lines[2].contains(";100;\"Outro app\";\"com.outro\";350;50;400;40,00"))
    }

    @Test fun `identifica origem cache no arquivo`() {
        val lines = csv(sample(source = ReportSource.CACHE))
        assertTrue(lines.contains("\"CACHE\""))
        assertTrue(!lines.contains("\"AO_VIVO\""))
    }

    @Test fun `neutraliza formulas e escapa aspas sem descartar o texto`() {
        val weird = AppUsage(
            AppDescriptor(300, "=HYPERLINK(\"url\")", "  =1+2;\"olá\"\nlinha"),
            1L, 0L, 0.001,
        )
        val text = csv(sample(apps = listOf(weird)))
        assertTrue(text.contains("\"'  =1+2;\"\"olá\"\"\nlinha\""))
        assertTrue(text.contains("\"'=HYPERLINK(\"\"url\"\")\""))
    }

    @Test fun `rejeita relatorio nao coletado`() {
        val empty = UsageReport.empty(UsagePeriod(0L, 1000L, "Hoje", "today"))
        try {
            UsageCsvExporter.bytes(empty)
            throw AssertionError("Era esperado erro sem captura")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("relatório válido"))
        }
    }
}
