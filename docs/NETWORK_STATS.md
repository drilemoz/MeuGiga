# NetworkStatsManager: comportamento e limites

## Fonte

O MeuGiga usa `NetworkStatsManager` desde o Android 6.0 (API 23). O acesso a outros aplicativos depende de `PACKAGE_USAGE_STATS`, concedido na tela oficial do Android.

## Rede móvel

Somente as chamadas do **leitor móvel** recebem `ConnectivityManager.TYPE_MOBILE`.
Os gráficos, histórico Room e alertas continuam ligados exclusivamente a ele.

O identificador do assinante é `null`. Desde Android 10, esse é o caminho compatível para consultar o total acessível das redes móveis sem pedir um identificador protegido do SIM.

## Totais e histórico

- `querySummaryForDevice`: total do aparelho no intervalo;
- `querySummary`: totais por UID;
- `queryDetails`: buckets históricos por UID.

As consultas rodam em `Dispatchers.IO`, pois o Android avisa que podem levar vários segundos.

## Buckets

O sistema não garante buckets de uma hora exata e não interpola buckets parciais. A interface usa rótulos como `08h`, `08–10h` ou intervalo entre datas conforme o início/fim real.

## UIDs

Um UID pode pertencer a mais de um pacote. Nessa situação, o MeuGiga mantém o consumo agregado do UID e indica que há pacotes compartilhados. UIDs do sistema, apps removidos ou pacotes invisíveis recebem um rótulo seguro.

## Diferenças para a operadora

O resultado pode divergir por:

- fuso e horário exato de renovação;
- arredondamento decimal;
- atraso do Android;
- tethering;
- tráfego contabilizado pela operadora fora do aparelho;
- redes/APNs especiais;
- restauração ou limpeza do histórico do sistema.

O MeuGiga é um monitor e não substitui o medidor oficial da operadora.


## Wi-Fi (aba separada, desde 0.1.6)

`AndroidWifiUsageReader` consulta `NetworkStatsManager` com
`ConnectivityManager.TYPE_WIFI` e `subscriberId = null`, agregando todas as
redes Wi-Fi disponíveis. `querySummaryForDevice` dá o total de download e
upload; `querySummary` oferece consumo por UID apenas se permitido.

A autorização especial `PACKAGE_USAGE_STATS` continua necessária. Se o total
estiver disponível e a listagem de UIDs falhar, a tela mostra o total e um
aviso de que o ranking não está disponível, sem inventar resultados. Se a
consulta do dispositivo falhar, não apresenta zeros como se fossem leitura.

As consultas Wi-Fi são **sob demanda** (abrir, selecionar período, atualizar).
Não gravam bytes no Room, não alimentam franquia/gráficos/alertas móveis e
não executam em background. Wi-Fi pode incluir tráfego de rede local; o
histórico depende do Android e não distingue SSIDs nem mede velocidade.

A versão 0.1.7 adiciona busca por nome/pacote/UID, ordenação local e detalhes
de cada UID na leitura atual. A diferença entre total do aparelho e soma dos
UIDs é informativa: se a soma supera o total, não exibimos um valor negativo
como se fosse tráfego não atribuído. Nem sempre as consultas são comparáveis.

Referência: https://developer.android.com/reference/android/app/usage/NetworkStatsManager
