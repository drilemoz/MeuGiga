package br.com.meugiga.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AlertThresholdsTest {
    @Test
    fun `default only when preference is missing`() {
        assertEquals(setOf(50, 75, 80, 90, 100), AlertThresholds.decode(null))
        assertEquals(emptySet<Int>(), AlertThresholds.decode(""))
        assertEquals(emptySet<Int>(), AlertThresholds.decode("abc"))
    }

    @Test
    fun `validates and sorts saved thresholds`() {
        val encoded = AlertThresholds.encode(setOf(-1, 100, 75, 101, 10))
        assertEquals("10,75,100", encoded)
        assertEquals(setOf(10, 75, 100), AlertThresholds.decode(encoded))
        assertEquals(setOf(50, 75), AlertThresholds.parse("50,75,75,nope,0,300"))
    }
}
