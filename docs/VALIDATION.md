# Validação da versão 0.1.7 (15/10/2026)

## Implementação

- Aba Wi-Fi compacta: indicador total em largura completa; download e upload alinhados.
- Atualização pelo botão do cabeçalho, mantendo consultas sob demanda.
- Ranking pesquisável por nome, pacote, pacote compartilhado ou UID; ordenável
  por total, download, upload ou nome sem consultar novamente a rede.
- Cada aplicativo abre uma página própria com download, upload, total, porcentagem,
  UID, pacote e aviso quando há múltiplos pacotes sob um UID.
- As barras do ranking não usam indicador circular de extremidade.
- Divergências de somatório por UID não geram tráfego fictício.
- Wi-Fi não persiste no Room, não entra em gráficos móveis, franquia ou WorkManager.

## Testes

- **47/47 testes Kotlin/JVM aprovados** em compilação isolada com `kotlinc`
  e executor local de asserções compatíveis com JUnit (38 anteriores + 9 novos).
- Nove testes novos aprovaram filtro, quatro ordenações, busca por nome/pacote/UID,
  lista vazia, diferença disponível e divergência não comparável.
- **66 arquivos Kotlin** (fontes e testes) analisados pelo parser PSI, sem erros sintáticos.
- Versões, XML/JSON, workflows e integridade do ZIP verificados na entrega.

## Pendências

- Android SDK e Gradle não disponíveis neste ambiente: build Android completo e
  testes instrumentados em aparelho permanecem pendentes.
- Testar rotação, cinco ícones de navegação, telas estreitas, nomes longos,
  acessibilidade, diferenças por UID e permissão revogada no Android real.

## Compatibilidade

- `applicationId`: `br.com.meugiga.app`.
- `versionName`: `0.1.7`, `versionCode`: `8`.
- Room schema `2`; histórico móvel, assinatura e permissões inalterados.
- O ZIP contém código-fonte, não um APK assinado.
