@echo off
echo Starting Shotlog Development Environment...
echo.

echo Building the application...
call mvnw clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo Build failed! Please check the errors above.
    pause
    exit /b 1
)

echo.
echo Starting Docker containers...
docker-compose -f docker-compose.dev.yml up --build

echo.
echo Development environment stopped.
pause