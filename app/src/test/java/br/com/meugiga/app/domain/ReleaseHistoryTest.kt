package br.com.meugiga.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseHistoryTest {
    @Test
    fun `latest changes show the current version first`() {
        assertEquals("0.1.9", ReleaseHistory.releases.first().version)
        assertTrue(ReleaseHistory.releases.first().changes.any { it.contains("Kotlin") })
    }

    @Test
    fun `about screen exposes only the last three versions in sequence`() {
        assertEquals(listOf("0.1.9", "0.1.8", "0.1.7"),
            ReleaseHistory.latestThree().map { it.version })
    }
}
