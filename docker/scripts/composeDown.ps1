Write-Host "Stopping containers..."
docker compose --env-file .\.env -f ./docker/compose.yaml down