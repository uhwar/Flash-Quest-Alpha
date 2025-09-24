@echo off
REM FlashQuest RPG Game Launcher
REM This script launches the FlashQuest application with proper Java and JavaFX setup

title FlashQuest RPG Game

echo ================================================
echo         FlashQuest - Educational RPG
echo ================================================
echo.

REM Check if Java is available
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or later to run FlashQuest
    echo Download from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Get Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
echo Found Java version: %JAVA_VERSION%

REM Check for required minimum version (Java 17)
for /f "tokens=1,2 delims=." %%a in ("%JAVA_VERSION:"=%") do (
    set MAJOR=%%a
    set MINOR=%%b
)

if %MAJOR% lss 17 (
    echo ERROR: Java 17 or later is required
    echo Current version: %JAVA_VERSION%
    echo Please update Java to version 17 or later
    echo.
    pause
    exit /b 1
)

echo Starting FlashQuest...
echo.

REM Set up JavaFX module path and add-modules
set JAVAFX_ARGS=--add-modules javafx.controls,javafx.fxml

REM Launch the application
java %JAVAFX_ARGS% -jar FlashQuest.jar

REM Check if the application exited with an error
if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: FlashQuest encountered an error during execution
    echo Exit code: %ERRORLEVEL%
    echo.
    echo Possible solutions:
    echo - Ensure all required files are present
    echo - Check if Java version is compatible
    echo - Verify JavaFX runtime is available
    echo.
    pause
)

echo.
echo FlashQuest has been closed.
pause