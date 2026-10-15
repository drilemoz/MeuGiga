package br.com.meugiga.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PixPayloadTest {
    @Test fun `qr pix sem valor permite escolha no banco`() {
        val code = PixPayload.build()
        assertTrue(code.contains("BR.GOV.BCB.PIX"))
        assertTrue(code.contains(PixPayload.PIX_KEY))
        assertFalse(code.contains("540"))
        assertEquals(PixPayload.crc16(code.dropLast(4)), code.takeLast(4))
    }
    @Test fun `qr pix com valor sugerido tem centavos`() {
        val code = PixPayload.build(BigDecimal("10.10"))
        assertTrue(code.contains("540510.10"))
        assertEquals(PixPayload.crc16(code.dropLast(4)), code.takeLast(4))
    }
    @Test fun `crc segue o vetor padrao CCITT FALSE`() {
        assertEquals("29B1", PixPayload.crc16("123456789"))
    }
    @Test fun `campos emv validos`() {
        val code = PixPayload.build()
        assertTrue(code.startsWith("000201"))
        assertTrue(code.endsWith(code.takeLast(4)))
        assertTrue(code.contains("5802BR"))
    }
    @Test fun `valores invalidos sao recusados`() {
        assertNull(PixPayload.parseAmount("0"))
        assertNull(PixPayload.parseAmount("-5"))
        assertNull(PixPayload.parseAmount("10,999"))
        assertNull(PixPayload.parseAmount("xyz"))
        assertEquals(BigDecimal("5.00"), PixPayload.parseAmount("5"))
        assertEquals(BigDecimal("10.10"), PixPayload.parseAmount("10,10"))
    }
}
