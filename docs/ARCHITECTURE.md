# Arquitetura do MeuGiga

## Fluxo principal

1. `MainViewModel` calcula o ciclo e o período selecionado.
2. `UsageRepository` solicita uma leitura a `MobileUsageReader`.
3. `AndroidNetworkStatsReader` consulta apenas `TYPE_MOBILE` fora da thread principal.
4. Os totais por UID formam o ranking; os buckets preservam o histórico.
5. Room recebe upserts e caches por período.
6. `PackageCatalog` associa apenas aplicativos visíveis sem `QUERY_ALL_PACKAGES`.
7. O ViewModel publica um `MainUiState` imutável para o Compose.

## Aba Wi-Fi independente

`WifiViewModel` só executa `WifiUsageRepository.load()` após abrir a rota
`wifi`, trocar um período ou tocar em Atualizar. O `WifiPeriodCalculator` cria
períodos sem relação com o ciclo da operadora. `AndroidWifiUsageReader`
consulta apenas `TYPE_WIFI` usando `querySummaryForDevice` (total) e, quando
possível, `querySummary` (UIDs). `WifiUsageCalculator` agrega e classifica o
ranking sem somar `UID_ALL` e usa o total do dispositivo como denominador.

O `WifiRankingPresentation` pesquisa e ordena o ranking localmente e calcula a diferença
entre total do dispositivo e soma dos aplicativos apenas se não houver sobreposição
numérica. `WifiAppDetailScreen` exibe o UID selecionado sem consultar os dados
móveis nem construir um histórico Wi-Fi.

O relatório Wi-Fi só fica no StateFlow em memória. Não usa Room para bytes,
nem o fluxo móvel de cache, gráficos, franquia, notificações ou WorkManager.
`PackageCatalog` é compartilhado apenas para resolver nomes de aplicativos.

## Exportação local

`UsageCsvExporter` transforma o `UsageReport` selecionado em CSV UTF-8 (BOM,
separador `;` e números de bytes inteiros). A tela Aplicativos oferece o comando,
a Activity usa `CreateDocument("text/csv")` do Storage Access Framework e
escreve no `Uri` escolhido no dispatcher de I/O. Não há permissão de armazenamento
nem rede adicional. A seleção pendente é retida no ViewModel durante rotação.
Nomes/pacotes recebem escape CSV e neutralização de células iniciadas por fórmulas.
Sem relatórios válidos, a operação não inicia.

## Responsabilidades

- `AndroidNetworkStatsReader`: leitura exclusiva da rede móvel.
- `AndroidWifiUsageReader`: consulta isolada de Wi-Fi, sob demanda.
- `UsageAccessController`: verificação e Intent do acesso especial.
- `MobileUsageDao`: persistência eficiente e limpeza de retenção.
- `UsageRepository`: cache, fallback e relatórios.
- `PlanCalculator`: ciclos e métricas do plano.
- `PeriodCalculator`: filtros e limites temporais.
- `UsageAggregator`: download/upload por intervalo e UID.
- `SettingsRepository`: plano, onboarding, tema, frequência e alertas.
- `UsageWorkScheduler`: periodicidade oficial do WorkManager.
- `UsageNotificationManager`: canais, status e limiares já enviados.
- `MainViewModel`: coordenação; não contém consultas SQL nem Android NetworkStats.
- `UsageCsvExporter`: formatação e escape do relatório sem dependência Android.

## Estratégia de bateria

- totais em primeiro plano: aproximadamente 60 segundos;
- histórico detalhado em primeiro plano: no máximo a cada 15 minutos, salvo atualização manual;
- segundo plano: desligado (0) ou 15, 30 ou 60 minutos; padrão de 30 somente após onboarding e permissão de uso;
- o agendamento é cancelado quando desativado ou sem permissão; o worker valida novamente o estado antes de consultar;
- trabalho periódico exige bateria não baixa;
- nenhuma restrição de rede é aplicada, pois a fonte é local;
- nenhum foreground service é usado;
- nenhum polling continua após a Activity sair do estado iniciado; com segundo plano desativado, a notificação de status é cancelada ao sair para evitar leitura desatualizada.

## Persistência

`mobile_usage_buckets` usa a chave composta `(uid, startMillis, endMillis)`. Uma nova leitura do mesmo bucket atualiza seus bytes e não cria outra linha.

`usage_queries` e `usage_query_apps` armazenam o último resumo exato de cada chave de período. Isso permite abrir o app sem permissão ou quando o Android temporariamente não responde.

A retenção padrão é de 120 dias. Buckets entre dois dias são exibidos com ambos os dias no gráfico, sem distribuir os bytes por suposição.

## Evolução

Migrações futuras do Room devem ser explícitas. Não usar `fallbackToDestructiveMigration`, pois o histórico é parte central do produto.

