# Publicacao automatica no Google Play

Este projeto esta preparado para publicar o AAB na trilha de teste interno usando a Google Play Developer API.

## 1. Google Cloud

1. Abra o Google Cloud Console.
2. Crie ou selecione um projeto.
3. Ative a API: **Google Play Android Developer API**.
4. Crie uma **Conta de servico**.
5. Gere uma chave JSON para essa conta.
6. Salve o arquivo na raiz deste projeto com o nome:

```text
play-service-account.json
```

Esse arquivo esta no `.gitignore` e nao deve ir para o GitHub.

## 2. Play Console

1. Entre no Play Console.
2. Va em **Usuarios e permissoes**.
3. Convide a conta de servico criada no Google Cloud.
4. Conceda permissao para o app **Check Mercado**.
5. Permissao minima recomendada: gerenciar releases na trilha de teste interno.

## 3. Publicar no teste interno

Antes de publicar uma nova versao, aumente o `versionCode` em:

```text
app/build.gradle
```

Depois rode:

```powershell
.\publish-play-internal.ps1
```

O comando gera/publica o bundle release na trilha `internal`.

## Observacoes

- O pacote do app e `com.codex.compralink`.
- O envio usa App Bundle (`.aab`), como a Play Store exige.
- Nao use esta chave JSON em outro computador sem necessidade.
- Se a chave vazar, revogue-a no Google Cloud e gere outra.
