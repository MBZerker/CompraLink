$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$credentials = Join-Path $root "play-service-account.json"
if (!(Test-Path $credentials)) {
    Write-Host "Arquivo play-service-account.json nao encontrado na raiz do projeto." -ForegroundColor Yellow
    Write-Host "Baixe a chave JSON da conta de servico e salve como:" -ForegroundColor Yellow
    Write-Host $credentials -ForegroundColor Cyan
    exit 1
}

$jdk = Resolve-Path ".tools\jdk17\jdk-17.0.19+10"
$sdk = Resolve-Path ".android-sdk"
$env:JAVA_HOME = $jdk.Path
$env:ANDROID_HOME = $sdk.Path
$env:ANDROID_SDK_ROOT = $sdk.Path
$env:Path = "$($env:JAVA_HOME)\bin;$env:Path"

& ".tools\gradle-8.10.2\bin\gradle.bat" --no-daemon --console=plain :app:publishReleaseBundle
