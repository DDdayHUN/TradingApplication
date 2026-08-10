Write-Host "Removing DB volume..."
docker compose --env-file .\.env -f docker/compose.yaml down -v trading-db

Write-Host "Restarting DB..."
docker compose --env-file .\.env -f docker/compose.yaml up -d trading-db