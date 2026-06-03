# Obtiene un JWT de la API via login.
# Uso:   .\get-token.ps1 [-Username alice] [-Password Password123!]
param(
    [string]$Username = "alice",
    [string]$Password = "Password123!"
)

$body = @{
    username = $Username
    password = $Password
} | ConvertTo-Json

$response = Invoke-RestMethod `
    -Uri "http://localhost:8082/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

$response.accessToken
