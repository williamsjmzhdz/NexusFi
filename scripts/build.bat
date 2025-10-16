@echo off
REM NexusFi - Build Script
REM This script cleans and compiles the project

echo.
echo ========================================
echo   NexusFi - Building Project
echo ========================================
echo.

call mvn clean compile

echo.
echo Build completed.
pause
