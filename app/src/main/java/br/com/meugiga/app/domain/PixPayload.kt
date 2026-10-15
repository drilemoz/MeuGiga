package br.com.meugiga.app.domain

import java.math.BigDecimal
import java.math.RoundingMode

/** Pix estático, EMV/BR Code. O banco valida o titular antes de confirmar o pagamento. */
object PixPayload {
    const val PIX_KEY = "drilemoz@gmail.com"
    // Dados de identificação do QR, não substituem a confirmação do titular pelo banco.
    private const val MERCHANT_NAME = "ADRIEDSON LEMOS"
    private const val MERCHANT_CITY = "IGUATAMA"

    fun parseAmount(input: String): BigDecimal? {
        val normalized = input.trim().replace(',', '.')
        return runCatching { BigDecimal(normalized).setScale(2, RoundingMode.UNNECESSARY) }
            .getOrNull()?.takeIf { it > BigDecimal.ZERO && it <= BigDecimal("999999.99") }
    }

    fun build(amount: BigDecimal? = null): String {
        require(amount == null || amount > BigDecimal.ZERO && amount <= BigDecimal("999999.99") && amount.scale() <= 2)
        val account = field("00", "BR.GOV.BCB.PIX") + field("01", PIX_KEY)
        val beforeCrc = buildString {
            append(field("00", "01"))
            append(field("26", account))
            append(field("52", "0000"))
            append(field("53", "986"))
            if (amount != null) append(field("54", amount.setScale(2).toPlainString()))
            append(field("58", "BR"))
            append(field("59", MERCHANT_NAME))
            append(field("60", MERCHANT_CITY))
            append(field("62", field("05", "***")))
            append("6304")
        }
        return beforeCrc + crc16(beforeCrc)
    }

    private fun field(id: String, value: String): String {
        val size = value.toByteArray(Charsets.UTF_8).size
        require(size <= 99)
        return id + size.toString().padStart(2, '0') + value
    }

    /** CRC-16/CCITT-FALSE: polinômio 0x1021, init 0xFFFF. */
    fun crc16(input: String): String {
        var crc = 0xFFFF
        input.toByteArray(Charsets.UTF_8).forEach { b ->
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1
                crc = crc and 0xFFFF
            }
        }
        return crc.toString(16).uppercase().padStart(4, '0')
    }
}
