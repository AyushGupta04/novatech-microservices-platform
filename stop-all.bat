@echo off
title Novamart Shutdown
echo ========================================================
echo   Stopping Novamart Platform
echo ========================================================
echo.

echo Stopping Docker containers...
docker compose down

echo.
echo Stopping any running Spring Boot / Java services...
taskkill /F /IM java.exe >nul 2>&1

echo Stopping any running Node/Vite frontend servers...
taskkill /F /IM node.exe >nul 2>&1

echo.
echo ========================================================
echo   All Novamart services stopped successfully.
echo ========================================================
pause
