# CompraLink

App Android de lista de compras com varias listas, precos por produto, itens riscados ao concluir e compartilhamento por link.

## Play Protect

A versao 1.2 removeu o fluxo de instalacao interna de APK para reduzir alertas do Play Protect. O app nao usa mais `REQUEST_INSTALL_PACKAGES`, nao baixa APK em cache e nao tenta abrir o instalador sozinho. A verificacao de update apenas abre o GitHub para o usuario baixar a nova versao.

## Compartilhamento

O botao **Compartilhar** aparece apenas dentro de uma lista aberta e envia um link HTTPS clicavel no WhatsApp. O payload e comprimido antes de entrar na URL:

```text
https://compralink.app/l/...
```

Quando o outro celular tiver o CompraLink instalado e abrir esse link, a lista e salva automaticamente como uma nova lista.

## Comparacao de precos

Toque no nome azul de um produto para ver precos mais baratos ja salvos em outras listas. Toque no preco para editar. O checkbox so marca/desmarca quando o quadradinho e tocado.

## Atualizacao pelo git

O app consulta:

```text
https://raw.githubusercontent.com/MBZerker/CompraLink/main/update.json
```

Se `versionCode` for maior que o instalado, ele pergunta se deseja atualizar e abre a pagina do GitHub. O Android fica responsavel pela confirmacao de instalacao.

## Build

```powershell
.\gradlew.bat assembleDebug
```

APK:

```text
CompraLink-debug.apk
```
