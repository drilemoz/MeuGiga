package br.com.meugiga.app.domain

data class ReleaseNote(val version: String, val changes: List<String>)

/** Fonte única das últimas novidades na tela automática e no histórico manual. */
object ReleaseHistory {
    val releases = listOf(
        ReleaseNote("0.1.8", listOf(
            "Calendário para escolher um dia nos dados móveis e no Wi-Fi, mantendo os módulos separados.",
            "Me Pague um Café: Pix voluntário, QR Code local e opção de avaliar no Google Play.",
            "Diagnóstico de execução em segundo plano e acesso às configurações de bateria.",
            "Novidades consultáveis em Sobre, com as três versões mais recentes.",
        )),
        ReleaseNote("0.1.7", listOf(
            "Wi-Fi com cartões compactos, ranking pesquisável, ordenação e detalhes por aplicativo.",
        )),
        ReleaseNote("0.1.6", listOf(
            "Aba Wi-Fi independente com consumo por aplicativo e filtros de período.",
        )),
        ReleaseNote("0.1.5", listOf("Exportação de relatórios móveis em CSV.")),
    )
    fun latestThree(): List<ReleaseNote> = releases.take(3)
}
