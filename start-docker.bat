@echo off
title Novamart Enterprise Platform - Docker Launcher
echo ========================================================
echo   Starting Novamart Microservices Platform (Docker)
echo ========================================================
echo.

docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Desktop is not running!
    echo Please start Docker Desktop and run this script again.
    pause
    exit /b 1
)

echo [INFO] Building and starting all containers...
echo - MySQL 8 (auth_db, product_db, inventory_db, cart_db, order_db)
echo - Redis 7
echo - Auth Service (:8081)
echo - Product Service (:8082)
echo - Inventory Service (:8083)
echo - Cart Service (:8084)
echo - Order Service (:8085)
echo - API Gateway (:8080)
echo - React Frontend (:3000)
echo.

docker compose up --build

pause
