Write-Host "Strating containers..."
docker compose --env-file .\.env -f ./docker/compose.yaml up -d

Write-Host "Strating IBKR IB Gateway..."
$possiblePaths = @(
    "C:\Jts\ibgateway\latest\ibgateway.exe"
)

$gatewayPath = $possiblePaths | Where-Object {Test-Path $_ } |
        Select-Object -First 1

if (-not $gatewayPath) {
    Write-Host "IB Gateway was not found in the common installation locations."
    Write-Host "Searching C:\Jts..."

    $gatewayPath = Get-ChildItem `
        -Path "C:\Jts" `
        -Filter "ibgateway.exe" `
        -Recurse `
        -ErrorAction SilentlyContinue |
            Select-Object -First 1 |
            Select-Object -ExpandProperty FullName
}

if (-not $gatewayPath) {
    Write-Host "Could not find IB Gateway."
    exit 1
}

Write-Host "Found IB Gateway at:"
Write-Host $gatewayPath

$gatewayProcess = Get-Process -Name "ibgateway" -ErrorAction SilentlyContinue

if ($gatewayProcess) {
    Write-Host "IB Gateway is already running."
    exit 0
}

Start-Process -FilePath $gatewayPath

Write-Host "IB Gateway started successfully."
