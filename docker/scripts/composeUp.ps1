Write-Host "Strating containers..."
docker compose --env-file .\.env -f ./docker/compose.yaml up -d