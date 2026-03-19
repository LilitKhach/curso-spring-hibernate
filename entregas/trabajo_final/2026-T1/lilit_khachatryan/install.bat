@echo off
REM Installation script for Analizador Financiero

echo ================================
echo Analizador Financiero Setup
echo ================================
echo.

REM Check if Node.js is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Node.js is not installed!
    echo Please download and install Node.js from https://nodejs.org/
    echo.
    echo Then run this script again.
    pause
    exit /b 1
)

echo [OK] Node.js found
node --version
npm --version
echo.

REM Install frontend dependencies
echo ================================
echo Installing Frontend Dependencies...
echo ================================
cd frontend
call npm install

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Frontend installation failed!
    pause
    exit /b 1
)

echo.
echo ================================
echo Installation Complete!
echo ================================
echo.
echo Next steps:
echo.
echo Option 1 - Local Development:
echo   Terminal 1: mvn spring-boot:run
echo   Terminal 2: cd frontend && npm start
echo.
echo Option 2 - Docker:
echo   docker-compose up -d
echo.
echo Access the application at:
echo   http://localhost:3000
echo.
pause

