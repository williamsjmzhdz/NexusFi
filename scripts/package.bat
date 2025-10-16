@echo off
REM NexusFi - Package Script
REM This script creates a runnable JAR file

echo.
echo ========================================
echo   NexusFi - Packaging Application
echo ========================================
echo.

call mvn clean package -DskipTests

echo.
echo Package created in target\ folder
pause
