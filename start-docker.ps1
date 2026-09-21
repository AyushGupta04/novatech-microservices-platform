Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Novamart Enterprise E-Commerce - Docker Launcher" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# Check Docker daemon
$dockerStatus = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker Desktop is not running. Please start Docker Desktop and retry." -ForegroundColor Red
    pause
    exit 1
}

Write-Host "[1/3] Building and starting all containers in detached mode..." -ForegroundColor Yellow
docker compose up --build -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker Compose failed to start services." -ForegroundColor Red
    pause
    exit 1
}

Write-Host ""
Write-Host "[2/3] Waiting for API Gateway and Frontend to initialize (15 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "[3/3] Launching Novamart Web Application in default browser..." -ForegroundColor Green
Start-Process "http://localhost:3000"

Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "  Novamart Platform is Running Successfully!" -ForegroundColor Green
Write-Host "  - Frontend:    http://localhost:3000" -ForegroundColor White
Write-Host "  - API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "  - Swagger UI:  http://localhost:8082/swagger-ui.html" -ForegroundColor White
Write-Host "========================================================" -ForegroundColor Green
Write-Host ""
Write-Host "To view real-time logs, run: docker compose logs -f" -ForegroundColor Gray
Write-Host "To stop all services, run:    docker compose down" -ForegroundColor Gray
Write-Host ""
pause
