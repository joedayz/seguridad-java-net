param()

$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim().Trim('"')
            Set-Item -Path "Env:$name" -Value $value
        }
    }
}

$tenantId = $env:AZURE_TENANT_ID
$clientId = $env:AZURE_CLIENT_ID
$scope = $env:AZURE_SCOPE

if (-not $tenantId -or -not $clientId -or -not $scope) {
    Write-Error "Define AZURE_TENANT_ID, AZURE_CLIENT_ID y AZURE_SCOPE en .env"
    exit 1
}

$deviceBody = @{
    client_id = $clientId
    scope     = "$scope openid profile offline_access"
}

$device = Invoke-RestMethod `
    -Uri "https://login.microsoftonline.com/$tenantId/oauth2/v2.0/devicecode" `
    -Method Post `
    -ContentType "application/x-www-form-urlencoded" `
    -Body $deviceBody

Write-Host "Abre $($device.verification_uri)"
Write-Host "Introduce el codigo: $($device.user_code)"
Write-Host "Esperando autenticacion..."

$interval = if ($device.interval) { $device.interval } else { 5 }

while ($true) {
    Start-Sleep -Seconds $interval
    try {
        $tokenBody = @{
            grant_type  = "urn:ietf:params:oauth:grant-type:device_code"
            client_id   = $clientId
            device_code = $device.device_code
        }
        $token = Invoke-RestMethod `
            -Uri "https://login.microsoftonline.com/$tenantId/oauth2/v2.0/token" `
            -Method Post `
            -ContentType "application/x-www-form-urlencoded" `
            -Body $tokenBody
        $token.access_token
        exit 0
    }
    catch {
        $err = $_.ErrorDetails.Message | ConvertFrom-Json -ErrorAction SilentlyContinue
        if ($err.error -eq "authorization_pending") { continue }
        throw
    }
}
