package br.com.meugiga.app.domain

import br.com.meugiga.app.domain.model.AppDescriptor
import br.com.meugiga.app.domain.model.UidUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiUsageCalculatorTest {
    private val descriptors = mapOf(
        10001 to AppDescriptor(10001, "app.a", "App A"),
        10002 to AppDescriptor(10002, "app.b", "App B"),
    )

    @Test fun `agrega duplicatas por uid sem misturar total do aparelho`() {
        val apps = WifiUsageCalculator.appRanking(
            listOf(UidUsage(10001, 100, 10), UidUsage(10001, 20, 5), UidUsage(10002, 40, 10)),
            descriptors, 400,
        )
        assertEquals(2, apps.size)
        assertEquals(135L, apps[0].totalBytes)
        assertEquals(120L, apps[0].rxBytes)
        assertEquals(15L, apps[0].txBytes)
        assertEquals(50L, apps[1].totalBytes)
        assertEquals(135.0 / 400.0, apps[0].shareOfTotal, 0.0001)
    }

    @Test fun `nao conta bucket agregado de todos os uid como aplicativo`() {
        val apps = WifiUsageCalculator.appRanking(
            listOf(UidUsage(-1, 1000, 100), UidUsage(10001, 50, 0)),
            descriptors, 1150,
        )
        assertEquals(1, apps.size)
        assertEquals(10001, apps.single().uid)
    }

    @Test fun `ignora consumo nulo e percentual nao excede cem`() {
        val apps = WifiUsageCalculator.appRanking(
            listOf(UidUsage(10001, 200, 300), UidUsage(10002, 0, 0)),
            descriptors, 100,
        )
        assertEquals(1, apps.size)
        assertEquals(1.0, apps.single().shareOfTotal, 0.0)
    }

    @Test fun `suporta lista vazia sem inventar consumo`() {
        assertTrue(WifiUsageCalculator.appRanking(emptyList(), descriptors, 0).isEmpty())
    }
}
