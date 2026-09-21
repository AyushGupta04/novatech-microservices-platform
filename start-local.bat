@echo off
title Novamart Local Launcher
echo ========================================================
echo   Starting Novamart Locally (Native Java & Vite)
echo ========================================================
echo.

echo [1/4] Starting MySQL 8 and Redis 7 in Docker...
docker compose up -d mysql redis
if errorlevel 1 (
    echo [ERROR] Failed to start MySQL/Redis containers. Ensure Docker Desktop is running.
    pause
    exit /b 1
)

echo.
echo [2/4] Verifying/Building backend JARs...
if not exist "product-service\target\product-service-1.0.0-SNAPSHOT.jar" (
    echo Compiling modules (first run)...
    call mvn clean install -DskipTests
)

echo.
echo [3/4] Launching microservices in separate console windows...
start "Auth Service (8081)" cmd /k "mvn spring-boot:run -pl auth-service"
timeout /t 3 /nobreak >nul

start "Product Service (8082)" cmd /k "mvn spring-boot:run -pl product-service"
start "Inventory Service (8083)" cmd /k "mvn spring-boot:run -pl inventory-service"
start "Cart Service (8084)" cmd /k "mvn spring-boot:run -pl cart-service"
start "Order Service (8085)" cmd /k "mvn spring-boot:run -pl order-service"
timeout /t 5 /nobreak >nul

start "API Gateway (8080)" cmd /k "mvn spring-boot:run -pl api-gateway"
timeout /t 3 /nobreak >nul

echo.
echo [4/4] Starting Frontend Dev Server...
start "Frontend (Vite 5173)" cmd /k "cd frontend && npm run dev"

timeout /t 4 /nobreak >nul
start http://localhost:5173

echo.
echo ========================================================
echo   All services launched in separate windows!
echo   Close windows or run stop-all.bat to stop.
echo ========================================================
pause
