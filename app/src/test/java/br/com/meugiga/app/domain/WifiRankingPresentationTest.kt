package br.com.meugiga.app.domain

import br.com.meugiga.app.domain.model.AppDescriptor
import br.com.meugiga.app.domain.model.AppUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiRankingPresentationTest {
    private val apps = listOf(
        AppUsage(AppDescriptor(1003, "br.exemplo.video", "Vídeo", listOf("br.exemplo.video")), 300, 10, .31),
        AppUsage(AppDescriptor(1001, "br.exemplo.backup", "Backup", listOf("br.exemplo.backup", "br.exemplo.backup2")), 10, 400, .41),
        AppUsage(AppDescriptor(1002, null, "Sistema Android"), 100, 50, .15),
    )

    @Test fun `ordena por total decrescente`() {
        assertEquals(listOf(1001, 1003, 1002), WifiRankingPresentation.filterAndSort(apps, "", WifiSortOrder.TOTAL).map { it.uid })
    }

    @Test fun `ordena por download decrescente`() {
        assertEquals(listOf(1003, 1002, 1001), WifiRankingPresentation.filterAndSort(apps, "", WifiSortOrder.DOWNLOAD).map { it.uid })
    }

    @Test fun `ordena por upload decrescente`() {
        assertEquals(listOf(1001, 1002, 1003), WifiRankingPresentation.filterAndSort(apps, "", WifiSortOrder.UPLOAD).map { it.uid })
    }

    @Test fun `ordena por nome crescente`() {
        assertEquals(listOf(1001, 1002, 1003), WifiRankingPresentation.filterAndSort(apps, "", WifiSortOrder.NAME).map { it.uid })
    }

    @Test fun `busca nome com acento e sem distinguir maiusculas`() {
        assertEquals(listOf(1003), WifiRankingPresentation.filterAndSort(apps, " VÍD ", WifiSortOrder.TOTAL).map { it.uid })
    }

    @Test fun `busca identificador do pacote e pacote compartilhado`() {
        assertEquals(listOf(1001), WifiRankingPresentation.filterAndSort(apps, "backup2", WifiSortOrder.NAME).map { it.uid })
    }

    @Test fun `busca por uid e retorna lista vazia quando nao encontra`() {
        assertEquals(listOf(1002), WifiRankingPresentation.filterAndSort(apps, "1002", WifiSortOrder.TOTAL).map { it.uid })
        assertEquals(emptyList<AppUsage>(), WifiRankingPresentation.filterAndSort(apps, "inexistente", WifiSortOrder.TOTAL))
    }

    @Test fun `calcula diferenca nao atribuida sem inventar dados`() {
        assertEquals(130L, WifiRankingPresentation.notAttributedBytes(1000L, apps, true))
        assertNull(WifiRankingPresentation.notAttributedBytes(1000L, apps, false))
    }

    @Test fun `nao apresenta diferenca negativa como outros`() {
        assertNull(WifiRankingPresentation.notAttributedBytes(100L, apps, true))
    }
}
