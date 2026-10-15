# Changelog

## 0.1.9 — 15/10/2026

- Correção dos erros de compilação observados nos logs Android CI #5 e Android Signed APK #6 da 0.1.8.
- `BackgroundDiagnostics.kt`: a lista anulável retornada pelo WorkManager agora é armazenada em `val` local antes da verificação de estado, evitando cinco erros de smart cast Kotlin.
- `InfoScreens.kt`: import de `fillMaxWidth` para os dois cartões clicáveis na tela Sobre.
- Preservados módulo Wi-Fi separado, seleção de datas, doações Pix, preferências, Room schema 2, workflows e assinatura existente.
- Sincronizados VERSION, Gradle, version.json, Novidades, README, validação e documentação de release; `versionName 0.1.9` e `versionCode 10`.


## 0.1.8 — 15/10/2026

- Calendário acessível por ícone nos filtros de consumo móvel e Wi-Fi, com seleção de um dia civil e dados do Android consultados no intervalo correto; Wi-Fi permanece separado.
- Nova área Sobre → Me Pague um Café: chave Pix, cópia com confirmação, valores R$ 5, R$ 10, R$ 10,10 e livre, QR Code Pix estático local e avaliação opcional no Google Play.
- Diagnóstico do trabalho WorkManager, otimização de bateria e data/hora da última consulta automática bem-sucedida, sem serviço permanente e sem solicitar privilégios especiais.
- Sobre agora permite abrir novidades manualmente e consultar últimas três versões; banner automático continua aparecendo uma vez por atualização.
- Mantidos applicationId, Room schema 2 e monitoramento móvel; versionName 0.1.8/versionCode 9.

## 0.1.7 — 15/10/2026

- reorganização compacta da aba Wi-Fi: total em destaque, download e upload lado a lado, ícones uniformes e atualização no cabeçalho;
- ranking Wi-Fi pesquisável por nome, pacote ou UID e ordenável por total, download, upload ou nome;
- cartões do ranking com barras contínuas sem ponto decorativo, detalhes por aplicativo acessíveis por toque;
- tela de detalhes Wi-Fi separada, com período, total, download, upload, UID, participação e observação sobre UIDs compartilhados;
- identificação transparente da diferença entre total do aparelho e soma de UIDs quando calculável, sem criar tráfego artificial;
- navegação inferior com ícones padronizados em 24 dp, sem alterar a funcionalidade das outras abas;
- sem mudanças na assinatura, permissões, schema Room 2, franquia ou gráficos de dados móveis;
- versão 0.1.7 / versionCode 8 e nove novos testes de ranking e reconciliação.

## 0.1.6 — 14/10/2026

- nova aba Wi-Fi na barra de navegação, sem gráficos e com total, download, upload, períodos Hoje/Ontem/7/30 dias e ranking por aplicativo;
- consultas com `TYPE_WIFI` independentes do leitor de dados móveis, executadas somente ao abrir a aba ou solicitar atualização;
- mantém franquia, alertas, gráficos, banco de histórico e WorkManager somente com dados móveis;
- tratamento de restrições de leitura do Android: total disponível mesmo quando o detalhamento por app estiver bloqueado, com aviso claro;
- testes dos períodos e agregação de consumo Wi-Fi, sem migrar Room schema 2;
- versionCode 7 e versionName 0.1.6 sincronizados com metadados, documentação e Novidades.

## 0.1.5 — 14/10/2026

- exportação CSV local na tela **Aplicativos**, para o período selecionado, sem permissão de armazenamento ou internet;
- inclui totais do aparelho, bytes recebidos/enviados, consumo por UID, rótulo/pacote, origem (ao vivo/cache) e horário de coleta;
- arquivo UTF-8 com BOM e `;` como delimitador, adequado para planilhas pt-BR; escapa aspas e neutraliza fórmulas em campos fornecidos por apps;
- exportação usa uma cópia do relatório selecionado e bloqueia dados vazios, antigos ou de filtro ainda não atualizado;
- cinco novos testes unitários do CSV, mantendo os testes anteriores;
- Room schema 2 e identificador Android inalterados, versionCode 6.


## 0.1.4 — 14/10/2026

- respeita a seleção intencionalmente vazia de limiares, sem repor alertas padrão;
- opção Desativado na sincronização periódica, cancelando o WorkManager e mantendo atualização ao abrir o app;
- não agenda consultas antes do onboarding ou sem permissão; worker verifica requisitos antes da execução; oculta a notificação fixa ao sair quando o segundo plano está desligado;
- gráfico diário preserva buckets que atravessam a meia-noite sem atribuí-los erroneamente ao primeiro dia;
- onboarding com rolagem em dispositivos compactos e contraste reforçado no cartão principal;
- CI passa a compilar e publicar o APK de debug como artifact; release publica APK e AAB em tag imutável por versão e commit, sem apagar releases antigas;
- novos testes unitários para limiares, política de sincronização e agrupamento diário;
- mantido Room schema 2, sem migração destrutiva; versionCode 5.


## 0.1.3 — 26/09/2026

- corrigida a média diária do Histórico para considerar somente os dias efetivamente cobertos pelo intervalo;
- “Ontem” deixa de ser contado incorretamente como dois dias;
- períodos personalizados encerrados à meia-noite deixam de ganhar um dia extra no divisor da média;
- cálculo movido para a camada de domínio e coberto por testes de regressão;
- versão e documentação sincronizadas para 0.1.3 / versionCode 4.

## 0.1.2 — 26/09/2026

- busca na tela Aplicativos por nome visível ou nome de pacote;
- botão para limpar a busca e mensagem específica quando nenhum aplicativo corresponde ao filtro;
- nova tela “Novidades da atualização”, exibida uma única vez após uma atualização real do app;
- controle das novidades por `versionCode`, preparado para versões futuras;
- correção na leitura do `NetworkStatsManager`: respostas nulas agora são tratadas como estatísticas indisponíveis e não sobrescrevem o cache com consumo zerado;
- versão e documentação sincronizadas para 0.1.2 / versionCode 3.

## 0.1.1 — 26/08/2026

- indicadores compactos em três colunas no resumo, aplicativos, histórico e detalhes;
- período completo e dias restantes no card do ciclo atual;
- início do plano/ciclo configurável e aplicado aos cálculos;
- identificação correta de aplicativos removidos, tethering, agregados e sistema;
- tema escuro harmonizado, com superfícies, contrastes e bordas consistentes;
- cache do catálogo de aplicativos, persistência de identidades e consultas menos repetidas;
- agregação fora da thread principal e redução de consultas detalhadas desnecessárias;
- índices do Room e migração preservando o histórico existente;
- release no GitHub Actions gera APK assinado + AAB, exige os Secrets de assinatura e valida o APK com `apksigner`.

## 0.1.0 — 25/08/2026

- primeira versão funcional do MeuGiga;
- monitoramento exclusivo de dados móveis;
- consumo total, download, upload, restante, média e previsão;
- ranking e detalhes por aplicativo;
- filtros Hoje, Ontem, 7 dias, 30 dias, Ciclo e Personalizado;
- histórico local com Room;
- plano limitado ou sem limite definido;
- atualização em primeiro plano e WorkManager em segundo plano;
- notificação de acompanhamento e alertas percentuais opcionais;
- onboarding curto, temas claro/escuro/sistema e telas de privacidade/sobre;
- CI para lint, testes e APK debug;
- workflow manual para AAB release assinado por GitHub Secrets.
