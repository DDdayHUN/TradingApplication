Write-Host "Stopping keycloak container..."
docker compose --env-file .\.env -f docker\compose.yaml stop keycloak

Write-Host "Exporting realm..."
docker compose --env-file .\.env -f docker\compose.yaml run --rm keycloak export --realm trading --dir /opt/keycloak/data/import

Write-Host "Restarting keycloak conatiner..."
docker compose --env-file .\.env -f .\docker\compose.yaml up -d keycloak