param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ComposeArgs
)

if ($ComposeArgs.Count -eq 0) {
    Write-Error "Uso: .\compose.ps1 up --build | down | ..."
    exit 1
}

function Invoke-Compose {
    param([string]$Command, [string[]]$Args)
    & $Command @Args
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if (Get-Command docker -ErrorAction SilentlyContinue) {
    docker compose version *> $null
    if ($LASTEXITCODE -eq 0) {
        Invoke-Compose "docker" @("compose") + $ComposeArgs
        exit 0
    }
}

if (Get-Command podman -ErrorAction SilentlyContinue) {
    podman compose version *> $null
    if ($LASTEXITCODE -eq 0) {
        Invoke-Compose "podman" @("compose") + $ComposeArgs
        exit 0
    }
    if (Get-Command podman-compose -ErrorAction SilentlyContinue) {
        Invoke-Compose "podman-compose" $ComposeArgs
        exit 0
    }
}

Write-Error "No se encontro docker compose, podman compose ni podman-compose."
exit 1
