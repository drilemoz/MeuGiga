# MeuGiga — publicação Android (0.1.8)

## CI (`android-ci.yml`)

Em push, pull request ou execução manual, o workflow roda `lintDebug`,
`testDebugUnitTest` e `assembleDebug`. O APK não assinado para distribuição
(padrão debug) fica disponível como artifact por 14 dias.

## Release (`android-release.yml`)

Em pushes para `main`/`master` ou execução manual:

1. Valida os quatro Secrets de assinatura.
2. Reconstrói o keystore apenas no diretório temporário.
3. Executa testes, lint, `assembleRelease` e `bundleRelease`.
4. Valida o APK com `apksigner`.
5. Publica os artifacts `MeuGiga-0.1.8-release-signed.apk` e
   `MeuGiga-0.1.8-release-signed.aab` por 30 dias no workflow.
6. Cria prerelease GitHub com tag `meugiga-v0.1.8-<SHA7>`;
   não apaga nem substitui releases anteriores.

Se um mesmo commit for executado novamente, a prerelease existente é
preservada. A falha ao criar uma nova prerelease não elimina o artifact
que já foi carregado no workflow.

## Secrets obrigatórios

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

**Importante:** mantenha o mesmo keystore das versões distribuídas. Uma troca
de assinatura pode impedir atualização do APK instalado. Não inclua tokens,
keystores ou senhas no código-fonte e no ZIP público.
