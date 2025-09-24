@echo off
title FlashQuest Build Script

echo ================================================
echo         FlashQuest Build Script
echo ================================================
echo.

REM Check if Maven is available
mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo.
    echo To install Maven:
    echo 1. Download from https://maven.apache.org/
    echo 2. Extract to a folder (e.g., C:\apache-maven-3.9.x)
    echo 3. Add the bin folder to your PATH environment variable
    echo 4. Restart this command prompt and try again
    echo.
    pause
    exit /b 1
)

echo Maven is available, proceeding with build...
echo.

REM Clean and build the project
echo Building FlashQuest (this may take a few minutes)...
mvn clean package -DskipTests=true

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Build failed!
    echo Check the output above for error details.
    pause
    exit /b 1
)

echo.
echo Build successful!

REM Copy JAR to releases folder
if exist "target\FlashQuest-1.0.0.jar" (
    echo Copying JAR to releases folder...
    copy "target\FlashQuest-1.0.0.jar" "releases\standalone\" >nul
    echo ✓ FlashQuest-1.0.0.jar copied to releases\standalone\
) else (
    echo WARNING: FlashQuest-1.0.0.jar not found in target directory
)

echo.
echo ================================================
echo         Build Complete!
echo ================================================
echo.
echo Files created:
if exist "releases\standalone\FlashQuest-1.0.0.jar" (
    echo ✓ releases\standalone\FlashQuest-1.0.0.jar
) else (
    echo ✗ releases\standalone\FlashQuest-1.0.0.jar (MISSING)
)
echo ✓ releases\standalone\FlashQuest.bat
echo ✓ releases\standalone\README.txt

echo.
echo To run FlashQuest:
echo 1. Navigate to releases\standalone\
echo 2. Double-click FlashQuest.bat
echo.
echo Or test now by pressing any key...
pause >nul

cd releases\standalone
FlashQuest.bat