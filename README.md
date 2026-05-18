# CompraLink

App Android de lista de compras com varias listas, precos por produto, itens riscados ao concluir e compartilhamento por link.

## Compartilhamento

O botao **Compartilhar** envia um link HTTPS clicavel no WhatsApp:

```text
https://compralink.app/list?payload=...
```

Quando o outro celular tiver o CompraLink instalado e abrir esse link, a lista e salva automaticamente como uma nova lista.

## Comparacao de precos

Toque no nome azul de um produto para ver precos mais baratos ja salvos em outras listas. Toque no preco para editar. O checkbox so marca/desmarca quando o quadradinho e tocado.

## Atualizacao pelo git

O app consulta:

```text
https://raw.githubusercontent.com/MBZerker/CompraLink/main/update.json
```

Se `versionCode` for maior que o instalado, ele pergunta se deseja atualizar, baixa o APK indicado por `apkUrl` e abre o instalador do Android.

## Build

```powershell
.\gradlew.bat assembleDebug
```

APK:

```text
CompraLink-debug.apk
```
