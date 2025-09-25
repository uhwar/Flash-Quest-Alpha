@echo off
title FlashQuest Build Script

echo ================================================
echo         FlashQuest Build Script
echo ================================================
echo.

REM Set Maven path
set "MAVEN_PATH=C:\apache-maven\bin\mvn.cmd"

REM Check if Maven is available at expected location
if not exist "%MAVEN_PATH%" (
    echo ERROR: Maven not found at %MAVEN_PATH%
    echo.
    echo Checking if Maven is in PATH...
    mvn -version >nul 2>&1
    if %ERRORLEVEL% neq 0 (
        echo ERROR: Maven is not installed or not accessible
        echo.
        echo To fix this:
        echo 1. Install Maven from https://maven.apache.org/
        echo 2. Extract to C:\apache-maven (or update MAVEN_PATH in this script)
        echo 3. Or add Maven to your PATH environment variable
        echo.
        pause
        exit /b 1
    ) else (
        echo Found Maven in PATH, using 'mvn' command
        set "MAVEN_PATH=mvn"
    )
) else (
    echo Maven found at %MAVEN_PATH%
)

echo Proceeding with build...
echo.

REM Clean and build the project
echo Building FlashQuest (this may take a few minutes)...
"%MAVEN_PATH%" clean package -DskipTests=true

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