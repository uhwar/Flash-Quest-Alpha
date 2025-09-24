# Simple FlashQuest Build Script
# This script creates a basic JAR distribution without Maven

param(
    [string]$Version = "1.0.0"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    FlashQuest Simple Build Script      " -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Java
try {
    $javaVersion = java -version 2>&1
    Write-Host "✓ Java is available" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: Java is not installed" -ForegroundColor Red
    exit 1
}

# Create basic directories
Write-Host "📁 Creating directories..." -ForegroundColor Yellow
if (Test-Path "releases") {
    Remove-Item "releases" -Recurse -Force
}
New-Item -ItemType Directory -Path "releases/standalone" -Force | Out-Null

# Create a simple manifest file
Write-Host "📄 Creating manifest..." -ForegroundColor Yellow
$manifest = @"
Manifest-Version: 1.0
Main-Class: com.flashquest.FlashQuestApplication

"@
$manifest | Out-File -FilePath "MANIFEST.MF" -Encoding ASCII

# Create a simple launcher script 
Write-Host "🚀 Creating launcher..." -ForegroundColor Yellow
$launcherContent = @"
@echo off
title FlashQuest RPG Game

echo ================================================
echo         FlashQuest - Educational RPG
echo ================================================
echo.

java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or later to run FlashQuest
    pause
    exit /b 1
)

echo Starting FlashQuest...
java --add-modules javafx.controls,javafx.fxml -jar FlashQuest-$Version.jar

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: FlashQuest encountered an error
    echo Please check the console output above
    pause
)
"@

$launcherPath = "releases/standalone/FlashQuest.bat"
$launcherContent | Out-File -FilePath $launcherPath -Encoding ASCII
Write-Host "✓ Launcher created: $launcherPath" -ForegroundColor Green

# Create README
Write-Host "📝 Creating README..." -ForegroundColor Yellow
$readmeLines = @(
    "# FlashQuest v$Version - Standalone Distribution",
    "",
    "## Requirements", 
    "- Java 17 or later",
    "- JavaFX runtime (included in most modern Java distributions)",
    "",
    "## How to Run",
    "1. Double-click FlashQuest.bat to start the game",
    "2. Or run manually: java --add-modules javafx.controls,javafx.fxml -jar FlashQuest-$Version.jar", 
    "",
    "## Files",
    "- FlashQuest-$Version.jar - Main application file (PLACEHOLDER - needs Maven build)",
    "- FlashQuest.bat - Windows launcher script",
    "",
    "## Note",
    "This is a basic distribution structure. To complete the build:",
    "1. Install Maven (https://maven.apache.org/)",
    "2. Run: mvn clean package -DskipTests=true", 
    "3. Copy target/FlashQuest-$Version.jar to this directory",
    "",
    "For support, visit: https://github.com/flashquest/flashquest-rpg"
)

$readmePath = "releases/standalone/README.txt"
$readmeLines | Out-File -FilePath $readmePath -Encoding UTF8
Write-Host "✓ README created: $readmePath" -ForegroundColor Green

# Create build instructions
Write-Host "📋 Creating build instructions..." -ForegroundColor Yellow
$buildInstructions = @(
    "# FlashQuest Build Instructions",
    "",
    "## Prerequisites",
    "1. Java 17 or later",
    "2. Maven 3.6 or later",
    "",
    "## Build Steps", 
    "1. Open command prompt in project directory",
    "2. Run: mvn clean package -DskipTests=true",
    "3. Copy target/FlashQuest-$Version.jar to releases/standalone/",
    "4. Test by running FlashQuest.bat",
    "",
    "## Distribution Types",
    "- Standalone: JAR + launcher script in releases/standalone/",
    "- Portable: ZIP archive with all dependencies", 
    "- Installer: MSI installer for Windows (requires jpackage)",
    "",
    "## Maven Commands",
    "- mvn clean package: Build JAR",
    "- mvn assembly:single: Create portable ZIP",  
    "- mvn jpackage:jpackage: Create installer (if plugin available)"
)

$buildInstructionsPath = "BUILD.txt"
$buildInstructions | Out-File -FilePath $buildInstructionsPath -Encoding UTF8
Write-Host "✓ Build instructions created: $buildInstructionsPath" -ForegroundColor Green

# Clean up
Remove-Item "MANIFEST.MF" -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       Basic Structure Created!         " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 What was created:" -ForegroundColor White
Write-Host "   ✓ releases/standalone/ directory" -ForegroundColor Green
Write-Host "   ✓ FlashQuest.bat launcher script" -ForegroundColor Green
Write-Host "   ✓ README.txt documentation" -ForegroundColor Green
Write-Host "   ✓ BUILD.txt instructions" -ForegroundColor Green
Write-Host ""
Write-Host "🔨 To complete the build:" -ForegroundColor Yellow
Write-Host "   1. Install Maven from https://maven.apache.org/" -ForegroundColor Gray
Write-Host "   2. Add Maven to your PATH environment variable" -ForegroundColor Gray
Write-Host "   3. Run: mvn clean package -DskipTests=true" -ForegroundColor Gray
Write-Host "   4. Copy the generated JAR to releases/standalone/" -ForegroundColor Gray
Write-Host ""
Write-Host "🚀 Then you can distribute the releases/standalone/ folder!" -ForegroundColor Green
Write-Host ""