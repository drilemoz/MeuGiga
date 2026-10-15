# MeuGiga

**Seu consumo de dados, sem complicação.**

MeuGiga é um aplicativo Android nativo que acompanha o consumo de **dados móveis** e oferece uma consulta de **Wi-Fi em uma aba independente**. Wi-Fi não participa da franquia, alertas, gráficos ou histórico móvel. Não usa VPN, não bloqueia internet, não limpa o aparelho e não inclui publicidade ou rastreamento.

## Estado da versão 0.1.9

- Corrigida a compilação Kotlin dos workflows Android CI #5 e Signed APK #6: smart casts no diagnóstico WorkManager e import `fillMaxWidth` na tela Sobre.
- Atualização de correção sem alterações de persistência, permissões, assinatura ou fluxos de consumo móvel/Wi-Fi.
- Mantidas as funcionalidades introduzidas na 0.1.8:


- Calendário para consultar um dia de uso móvel ou Wi-Fi, com intervalos locais e módulos isolados.
- Sobre → Me Pague um Café com Pix estático e QR gerado localmente. Confira o destinatário em seu banco; nenhum pagamento é processado pelo aplicativo.
- Diagnóstico do agendamento WorkManager, orientações de bateria e últimos registros de sucesso automático.
- Histórico em Sobre com as três últimas versões; aviso de novidades uma vez por atualização.

- resumo compacto do ciclo atual com datas de início e fim;
- franquia limitada ou plano sem limite definido;
- total, restante, percentual, dias restantes, média diária, disponível por dia e previsão;
- download e upload separados;
- ranking por aplicativo, distinguindo apps removidos, tethering e sistema;
- busca de aplicativos por nome ou identificador de pacote;
- detalhes por aplicativo com histórico e intervalos fornecidos pelo Android;
- filtros Hoje, Ontem, 7 dias, 30 dias, Ciclo, Personalizado e calendário de dia único (também no Resumo);
- média diária calculada pelos dias efetivamente cobertos pelo período, sem duplicar o dia final;
- cache e histórico local em Room, mantendo buckets que atravessam a meia-noite como intervalos separados nos gráficos (sem divisão fictícia entre dias);
- atualização em primeiro plano a cada 60 segundos;
- atualização periódica configurável: desativada, 15, 30 ou 60 minutos; executada somente após onboarding e com permissão de estatísticas;
- notificação persistente opcional e alertas percentuais configuráveis, inclusive nenhum selecionado;
- tema claro, escuro ou seguindo o sistema; cartões principais com contraste reforçado;
- início do primeiro ciclo configurável, seguido da renovação recorrente;
- onboarding de três etapas com rolagem em telas compactas;
- tela de novidades exibida uma vez após cada atualização instalada;
- exportação manual de relatório CSV completo por período, via seletor de arquivos do Android, sem envio automático;
- aba **Wi-Fi** independente, com cartões compactos para total/download/upload, filtros Hoje/Ontem/7/30 dias e calendário de dia único, busca e ordenação de aplicativos e detalhes individuais por UID quando o Android permitir;
- Wi-Fi consultado apenas ao abrir a aba, mudar filtro ou atualizar, sem gráficos, persistência de histórico ou worker Wi-Fi;
- diferença entre total Wi-Fi e soma de aplicativos mostrada somente quando os dados permitem um cálculo não negativo;
- nenhum cadastro, login ou servidor.

## Tecnologias

- Kotlin 2.2.21;
- Jetpack Compose + Material 3;
- MVVM com Coroutines e StateFlow;
- Room 2.8.4;
- DataStore 1.2.1;
- NetworkStatsManager;
- UsageStatsManager e AppOpsManager para verificar o acesso especial;
- PackageManager com visibilidade limitada a apps iniciáveis;
- WorkManager 2.11.1;
- ZXing Core 3.5.3 para gerar QR Code Pix localmente;
- Android Gradle Plugin 8.13.2 e Gradle 8.13;
- minSdk 23, compileSdk 36 e targetSdk 36.

O `targetSdk 36` atende à regra anunciada para novos apps e atualizações enviados à Google Play a partir de 31/08/2026.

## Arquitetura

```text
br.com.meugiga.app
├── data
│   ├── database       Room, entidades e consultas móveis
│   ├── networkstats   leitores separados: TYPE_MOBILE e TYPE_WIFI
│   └── repository     catálogo de apps e relatórios separados
├── domain             ciclos, períodos, métricas e agregação
├── export             relatório CSV local e escape de campos
├── notification       notificação de status e alertas
├── settings           preferências locais em DataStore
├── ui                  Compose, tema, componentes e telas
├── viewmodel           estado e coordenação da interface
├── worker              sincronização periódica econômica
└── utils               formatação de bytes e datas
```

Detalhes em [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Como o NetworkStatsManager é usado

O leitor móvel `AndroidNetworkStatsReader` continua usando **somente**
`ConnectivityManager.TYPE_MOBILE`. Os gráficos, a franquia, o cache Room, os
alertas e o WorkManager continuam exclusivos da rede móvel.

O leitor separado `AndroidWifiUsageReader` usa `ConnectivityManager.TYPE_WIFI`
com `subscriberId = null`, para consultar **todas as redes Wi-Fi agregadas**.
A tela Wi-Fi permite Hoje, Ontem, 7 dias ou 30 dias, busca e ordenação, sem depender do ciclo do
plano. O total do aparelho vem de `querySummaryForDevice`, e o ranking por UID
vem de `querySummary`, caso disponibilizado pelo fabricante e permissão.
Não são atribuídos bytes de Wi-Fi a aplicativos móveis nem salvos históricos
Wi-Fi no Room. A consulta Wi-Fi ocorre somente quando a aba é aberta ou usada.

**Limitações:** o consumo Wi-Fi pode incluir tráfego LAN; não separa redes
por SSID, não mede velocidade Mbps e pode não corresponder à soma dos apps.
O Android pode restringir o ranking por UID mesmo que forneça o total. Os
buckets do sistema não oferecem resolução garantida ao minuto.

Mais detalhes em [docs/NETWORK_STATS.md](docs/NETWORK_STATS.md).

## Exportar relatório CSV

Em **Aplicativos**, selecione Hoje, Ontem, 7 dias, 30 dias, Ciclo ou Personalizado,
aguarde o término da atualização e toque em **Exportar relatório CSV**. O
seletor de arquivos do Android permite escolher o destino, inclusive um provedor
na nuvem já configurado pelo usuário. O próprio MeuGiga não acessa a internet.

O CSV é UTF-8 com BOM, separado por ponto e vírgula; contém todos os apps do
período selecionado independentemente do texto de busca, além dos totais do aparelho.
Os valores são **bytes** e as datas usam offset de fuso horário. `fim_exclusivo`
indica o limite final sem inclusão do instante terminal. `origem=CACHE` indica
leitura do cache local e não uma medição nova. O CSV não é um backup restaurável,
nem inclui todos os buckets do histórico: seu propósito é análise em planilha.

**Privacidade:** nomes de aplicativos e consumo ficam no arquivo que o usuário
salvar. Quem tiver acesso ao destino escolhido poderá ler o relatório.

## Permissões

### PACKAGE_USAGE_STATS

É uma autorização especial concedida manualmente em **Configurações > Acesso ao uso**. Permite consultar estatísticas de outros UIDs, incluindo uso de Wi-Fi quando liberado pelo sistema. O manifesto declara a intenção, mas somente o usuário pode concedê-la.

### POST_NOTIFICATIONS

Solicitada apenas quando o usuário ativa a notificação de acompanhamento ou os alertas no Android 13 ou superior.

### RECEIVE_BOOT_COMPLETED

Usada pelo WorkManager para restaurar o agendamento periódico após reinicializações.

O WorkManager também incorpora as permissões normais `WAKE_LOCK` e
`ACCESS_NETWORK_STATE` para executar e reagendar trabalho com segurança. Elas não
abrem telas de autorização, não permitem acesso à internet e não coletam conteúdo
de rede. A permissão e o serviço de primeiro plano trazidos genericamente pela
biblioteca são removidos no manifesto, pois o MeuGiga não promove seus workers a
foreground service.

O app **não** declara `INTERNET`, localização, telefone, VPN ou `QUERY_ALL_PACKAGES`.

## Compilar no Android Studio

1. Abra a pasta raiz `meugiga` no Android Studio compatível com AGP 8.13.
2. Instale o Android SDK Platform 36 quando solicitado.
3. Use JDK 17.
4. Sincronize o Gradle.
5. Execute a configuração `app` em um dispositivo Android 6.0 ou superior.

## Compilar pela linha de comando

```bash
./gradlew lintDebug testDebugUnitTest assembleDebug
```

APK gerado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

O pacote de entrega também pode incluir em `artifacts/` arquivos de validação local. APKs, AABs e credenciais de assinatura não devem ser versionados no Git.

Builds `release` exigem assinatura. Com as variáveis de assinatura configuradas, gere APK + AAB com:

```bash
./gradlew assembleRelease bundleRelease
```

## Testes

```bash
./gradlew testDebugUnitTest
```

Os testes cobrem:

- conversão e formatação de bytes;
- MB/GB para bytes;
- percentual, restante, média diária e previsão;
- ciclos com dia 31 em meses curtos;
- início configurado do ciclo e transição para renovações recorrentes;
- plano sem limite;
- Hoje e período personalizado;
- intervalo invertido;
- preservação de download/upload ao agregar buckets;
- separação do histórico por UID.
- classificação dos UIDs especiais expostos pelo Android.

## GitHub Actions

`android-ci.yml` executa lint, testes, compila `assembleDebug` e disponibiliza o APK debug nos Artifacts de pushes, pull requests e execução manual.

`android-release.yml` gera APK e AAB assinados automaticamente nos pushes de `main`/`master` ou manualmente, verifica a assinatura do APK e publica uma prerelease identificada pela versão e pelo commit, sem excluir releases anteriores. Configure estes GitHub Secrets:

- `ANDROID_KEYSTORE_BASE64`;
- `ANDROID_KEYSTORE_PASSWORD`;
- `ANDROID_KEY_ALIAS`;
- `ANDROID_KEY_PASSWORD`.

O keystore é reconstruído apenas no diretório temporário do runner e nunca deve ser commitado.

## Google Play

- applicationId: `br.com.meugiga.app`;
- ícone adaptativo e monocromático;
- release com minificação e redução de recursos;
- AAB preparado;
- política em [docs/PRIVACY_POLICY.md](docs/PRIVACY_POLICY.md);
- sem `QUERY_ALL_PACKAGES`;
- sem SDKs de anúncios ou analytics.

Antes da publicação, hospede a política de privacidade em uma URL pública, crie e proteja o keystore definitivo e preencha a seção Segurança dos dados de acordo com o comportamento da versão publicada.

## Limitações conhecidas

- o CSV exporta o relatório do período, mas **não é backup restaurável**; não existe restauração do banco local e dados do app podem ser perdidos em reinstalação/limpeza;
- se o monitoramento em segundo plano estiver desativado, alertas não serão atualizados com o aplicativo fechado e a notificação fixa é ocultada ao sair; as consultas continuam ao abri-lo;
- operadoras podem contabilizar tráfego de forma diferente do Android;
- tethering, apps removidos, UIDs compartilhados e tráfego do sistema podem aparecer agregados;
- alguns fabricantes atrasam ou restringem a atualização das estatísticas;
- WorkManager garante um intervalo mínimo, não um horário exato;
- a troca de SIM é tratada agregando todas as redes móveis disponíveis, sem coletar identificadores do assinante;
- o histórico anterior à instalação depende do que o Android ainda mantém no aparelho.

## Referências oficiais

- [NetworkStatsManager](https://developer.android.com/reference/android/app/usage/NetworkStatsManager)
- [Trabalho periódico com WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work)
- [Visibilidade de pacotes](https://developer.android.com/training/package-visibility/declaring)
- [Requisitos de target API da Google Play](https://developer.android.com/google/play/requirements/target-sdk)

## Me Pague um Café

A chave de Pix estático é `drilemoz@gmail.com`. O QR segue BR Code e usa identificação
provisória `ADRIEDSON LEMOS` / `IGUATAMA`. Antes da publicação/distribuição de
pagamentos reais, confirme que o nome e a cidade correspondem aos dados reais da conta;
o usuário deve verificar o beneficiário no banco antes de transferir. O aplicativo não
recebe confirmação de pagamento nem oferece benefícios em troca da contribuição.
O botão Google Play abre a ficha deste pacote, quando ela estiver disponível.
