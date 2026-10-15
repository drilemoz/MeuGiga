package br.com.meugiga.app.domain

/** Null means never configured; empty string means the user deselected every threshold. */
object AlertThresholds {
    val defaults: Set<Int> = setOf(50, 75, 80, 90, 100)

    fun decode(stored: String?): Set<Int> = if (stored == null) defaults else parse(stored)

    fun encode(thresholds: Set<Int>): String = thresholds
        .filter { it in 1..100 }
        .sorted()
        .joinToString(",")

    fun parse(raw: String?): Set<Int> = raw
        .orEmpty()
        .split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it in 1..100 }
        .toSet()
}
