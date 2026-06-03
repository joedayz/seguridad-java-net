# Obtiene un access token de Keycloak via Resource Owner Password Credentials.
# Uso:   .\get-token.ps1 [-Username alice] [-Password password]
# Ej:    .\get-token.ps1 -Username alice -Password password
#        .\get-token.ps1 -Username bob -Password password
param(
    [string]$Username = "alice",
    [string]$Password = "password"
)

$body = @{
    grant_type    = "password"
    client_id     = "demo-client"
    client_secret = "demo-secret"
    username      = $Username
    password      = $Password
}

$response = Invoke-RestMethod `
    -Uri "http://localhost:8080/realms/demo/protocol/openid-connect/token" `
    -Method Post `
    -ContentType "application/x-www-form-urlencoded" `
    -Body $body

$response.access_token
