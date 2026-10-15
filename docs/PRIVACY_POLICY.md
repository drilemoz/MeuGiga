# Política de Privacidade — MeuGiga

**Última atualização: 14 de outubro de 2026**

O MeuGiga foi desenvolvido com processamento local como princípio central.

## Dados tratados

O aplicativo consulta estatísticas de uso de dados móveis disponibilizadas pelo Android, incluindo quantidade recebida, quantidade enviada, intervalo de tempo e UID associado. Também lê nome e ícone de aplicativos visíveis para apresentar essas estatísticas de forma compreensível.

## Finalidade

Essas informações são usadas exclusivamente para mostrar consumo total, histórico, gráficos, ranking por aplicativo, métricas do plano e alertas escolhidos pelo usuário.

## Armazenamento

O histórico e as preferências ficam no armazenamento privado do aplicativo no próprio dispositivo. O MeuGiga não envia esse conteúdo para servidor externo.

## Dados não coletados

O MeuGiga não exige nem coleta cadastro, nome, e-mail, telefone, localização, contatos ou identificadores do assinante. Não monitora tráfego Wi‑Fi e não inclui publicidade ou SDK de rastreamento.

## Permissões

- **Acesso aos dados de uso:** necessário para consultar estatísticas de outros aplicativos. É concedido e revogado na tela oficial do Android.
- **Notificações:** usada somente quando o usuário ativa o acompanhamento ou alertas.
- **Inicialização do aparelho:** permite ao WorkManager restaurar atualizações periódicas.

## Compartilhamento

O MeuGiga não transmite automaticamente dados pessoais ou histórico de consumo a terceiros.
Ao escolher **Exportar relatório CSV**, o usuário determina a localização do arquivo
no seletor do Android. A cópia poderá ficar disponível a aplicativos ou serviços
com acesso ao local escolhido, inclusive serviços de nuvem, caso selecionados.
O CSV inclui nomes de apps, UIDs, pacotes e consumo; deve ser tratado como privado.

## Controle do usuário

O usuário pode revogar o acesso aos dados de uso e a permissão de notificações nas configurações do Android.
A exportação é opcional, iniciada manualmente e não exige permissões adicionais de armazenamento. A remoção do aplicativo apaga os dados mantidos em seu armazenamento privado, conforme o comportamento do sistema.

## Alterações

Esta política deve ser atualizada quando uma nova versão alterar materialmente o tratamento de dados.

## Contato

Antes da publicação na Google Play, o responsável pelo aplicativo deve inserir aqui um canal público de contato e hospedar esta política em uma URL acessível.



## Consulta Wi-Fi (a partir da 0.1.6)

O aplicativo também pode consultar estatísticas de consumo Wi-Fi agregadas
e por aplicativo, com a mesma autorização de acesso às estatísticas do
Android. Essa consulta só acontece ao acessar a aba Wi-Fi e solicitar uma
leitura. Os valores de tráfego Wi-Fi não são enviados a servidores, não são
armazenados no histórico Room e não entram nos gráficos ou alertas móveis.
O aplicativo não coleta SSID ou senha de Wi-Fi.
