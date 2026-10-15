# Validação da versão 0.1.6 (14/10/2026)

## Verificações locais

- **38/38 testes Kotlin/JVM aprovados** por compilação via `kotlinc` e executor
  local de asserções compatíveis com JUnit: são os 30 anteriores mais 8 novos
  testes de período e agregação Wi-Fi.
- O cálculo dos períodos Wi-Fi usa fuso local, trata Ontem como intervalo
  fechado no início e aberto no fim, inclui o dia atual nos filtros 7/30 dias.
- O ranking agrega UIDs repetidos, exclui UID_ALL e não soma bytes do aparelho
  aos bytes de aplicativos, evitando duplicação.
- Verificações adicionais: sintaxe Kotlin via PSI, consistência de versões,
  XML/JSON, YAML dos workflows, estrutura do ZIP e checksum SHA-256.

## Pendente

- Sem Android SDK configurado no ambiente de entrega: `assembleDebug`, lint
  Android, `assembleRelease`, `bundleRelease` e testes em aparelho não
  foram executados. O GitHub Actions contém as etapas de CI Android.
- Testar `querySummaryForDevice(TYPE_WIFI)` e `querySummary(TYPE_WIFI)` em aparelhos
  e versões Android reais, inclusive cenários de permissão negada/revogada,
  pouca memória, ausência de estatísticas ou bloqueio do ranking por UID.
- Verificar comportamento da navegação com cinco abas em telas estreitas,
  rotação de tela e acessibilidade.

## Compatibilidade

- `applicationId`: `br.com.meugiga.app` (inalterado).
- `versionName`: `0.1.6`, `versionCode`: `7`.
- Room schema `2`, sem migração ou exclusão de histórico móvel.
- O ZIP é código-fonte, não APK instalável; assinar releases com a mesma chave.
