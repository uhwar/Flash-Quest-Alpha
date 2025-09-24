# FlashQuest Release Build Script
# This script builds all distribution versions of FlashQuest

param(
    [switch]$Clean = $false,
    [switch]$SkipTests = $false,
    [string]$Version = "1.0.0"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "      FlashQuest Release Builder        " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set error action preference
$ErrorActionPreference = "Stop"

# Check if Maven is available
try {
    $null = mvn -version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Maven not found"
    }
    Write-Host "✓ Maven is available" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Maven and add it to your PATH" -ForegroundColor Yellow
    exit 1
}

# Check if Java is available
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✓ Java is available: $($javaVersion.Line.Split(' ')[2])" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 17 or later" -ForegroundColor Yellow
    exit 1
}

# Clean previous builds if requested
if ($Clean) {
    Write-Host "🧹 Cleaning previous builds..." -ForegroundColor Yellow
    if (Test-Path "target") {
        Remove-Item "target" -Recurse -Force
    }
    if (Test-Path "releases") {
        Remove-Item "releases" -Recurse -Force
    }
    New-Item -ItemType Directory -Path "releases" -Force | Out-Null
    New-Item -ItemType Directory -Path "releases/installer" -Force | Out-Null
    New-Item -ItemType Directory -Path "releases/portable" -Force | Out-Null  
    New-Item -ItemType Directory -Path "releases/standalone" -Force | Out-Null
    Write-Host "✓ Cleaned previous builds" -ForegroundColor Green
}

# Ensure releases directory exists
if (-not (Test-Path "releases")) {
    New-Item -ItemType Directory -Path "releases" -Force | Out-Null
    New-Item -ItemType Directory -Path "releases/installer" -Force | Out-Null
    New-Item -ItemType Directory -Path "releases/portable" -Force | Out-Null
    New-Item -ItemType Directory -Path "releases/standalone" -Force | Out-Null
}

Write-Host ""
Write-Host "🏗️  Building FlashQuest v$Version..." -ForegroundColor Cyan

# Step 1: Compile and package the application
Write-Host "📦 Step 1: Compiling and packaging..." -ForegroundColor Yellow
try {
    if ($SkipTests) {
        mvn clean package -DskipTests=true
    } else {
        mvn clean package
    }
    if ($LASTEXITCODE -ne 0) {
        throw "Maven package failed"
    }
    Write-Host "✓ Successfully compiled and packaged" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: Failed to compile and package the application" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

# Step 2: Create standalone JAR
Write-Host "📄 Step 2: Creating standalone JAR..." -ForegroundColor Yellow
try {
    # Copy the shaded JAR to releases/standalone
    $sourceJar = "target/FlashQuest-$Version.jar"
    $destJar = "releases/standalone/FlashQuest-$Version.jar"
    
    if (Test-Path $sourceJar) {
        Copy-Item $sourceJar $destJar -Force
        Write-Host "✓ Standalone JAR created: $destJar" -ForegroundColor Green
    } else {
        throw "Source JAR not found: $sourceJar"
    }
} catch {
    Write-Host "✗ ERROR: Failed to create standalone JAR" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Step 3: Create portable distribution
Write-Host "📁 Step 3: Creating portable distribution..." -ForegroundColor Yellow
try {
    # Use Maven assembly plugin to create portable ZIP
    mvn assembly:single -DskipTests=true
    if ($LASTEXITCODE -ne 0) {
        throw "Maven assembly failed"
    }
    
    # Check for the ZIP file
    $sourceZip = "releases/FlashQuest-$Version-portable.zip"
    if (Test-Path $sourceZip) {
        Write-Host "✓ Portable distribution created: $sourceZip" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Portable ZIP not found in expected location" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ ERROR: Failed to create portable distribution" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Step 4: Create launcher scripts
Write-Host "🚀 Step 4: Creating launcher scripts..." -ForegroundColor Yellow
try {
    # Create Windows launcher for standalone
    $launcherContent = "@echo off`r`n" +
    "title FlashQuest RPG Game`r`n`r`n" +
    "echo ================================================`r`n" +
    "echo         FlashQuest - Educational RPG`r`n" +
    "echo ================================================`r`n" +
    "echo.`r`n`r`n" +
    "java -version >nul 2>&1`r`n" +
    "if %ERRORLEVEL% neq 0 (`r`n" +
    "    echo ERROR: Java is not installed or not in PATH`r`n" +
    "    echo Please install Java 17 or later to run FlashQuest`r`n" +
    "    pause`r`n" +
    "    exit /b 1`r`n" +
    ")`r`n`r`n" +
    "echo Starting FlashQuest...`r`n" +
    "java --add-modules javafx.controls,javafx.fxml -jar FlashQuest-$Version.jar`r`n`r`n" +
    "if %ERRORLEVEL% neq 0 (`r`n" +
    "    echo.`r`n" +
    "    echo ERROR: FlashQuest encountered an error`r`n" +
    "    echo Please check the console output above`r`n" +
    "    pause`r`n" +
    ")"
    
    $launcherPath = "releases/standalone/FlashQuest.bat"
    $launcherContent | Out-File -FilePath $launcherPath -Encoding ASCII -NoNewline
    Write-Host "✓ Launcher script created: $launcherPath" -ForegroundColor Green
    
    # Create README for standalone
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
        "- FlashQuest-$Version.jar - Main application file",
        "- FlashQuest.bat - Windows launcher script",
        "",
        "## Troubleshooting",
        "If the game doesn't start:",
        "1. Verify Java 17+ is installed: java -version",
        "2. Check if JavaFX is available in your Java distribution", 
        "3. Try running the JAR file manually with the command above",
        "",
        "For support, visit: https://github.com/flashquest/flashquest-rpg"
    )
    
    $readmePath = "releases/standalone/README.txt"
    $readmeLines | Out-File -FilePath $readmePath -Encoding UTF8
    Write-Host "✓ README created: $readmePath" -ForegroundColor Green
    
} catch {
    Write-Host "⚠️  Warning: Failed to create launcher scripts" -ForegroundColor Yellow
    Write-Host $_.Exception.Message -ForegroundColor Yellow
}

# Step 5: Attempt to create Windows installer (optional)
Write-Host "🔧 Step 5: Attempting to create Windows installer..." -ForegroundColor Yellow
try {
    # Check if jpackage is available
    $null = jpackage --help 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "jpackage is available, attempting to create installer..." -ForegroundColor Green
        
        # Create a simple jpackage command
        $jpackageArgs = @(
            "--input", "releases/standalone"
            "--name", "FlashQuest"
            "--main-jar", "FlashQuest-$Version.jar"
            "--main-class", "com.flashquest.FlashQuestApplication"
            "--type", "msi"
            "--dest", "releases/installer"
            "--app-version", $Version
            "--vendor", "FlashQuest Team"
            "--description", "A gamified flashcard learning application with RPG progression mechanics"
            "--win-dir-chooser"
            "--win-shortcut"
            "--win-menu"
            "--win-menu-group", "Games"
        )
        
        & jpackage @jpackageArgs
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Windows installer created successfully" -ForegroundColor Green
        } else {
            throw "jpackage failed with exit code $LASTEXITCODE"
        }
    } else {
        Write-Host "⚠️  jpackage not available, skipping installer creation" -ForegroundColor Yellow
        Write-Host "   To create installers, use JDK 14+ with jpackage included" -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️  Warning: Failed to create Windows installer" -ForegroundColor Yellow
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Gray
    Write-Host "   The standalone and portable versions are still available" -ForegroundColor Gray
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           Build Complete!              " -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "📋 Build Summary:" -ForegroundColor White
Write-Host "   Version: $Version" -ForegroundColor Gray

if (Test-Path "releases/standalone/FlashQuest-$Version.jar") {
    Write-Host "   ✓ Standalone JAR: releases/standalone/" -ForegroundColor Green
}

if (Test-Path "releases/FlashQuest-$Version-portable.zip") {
    Write-Host "   ✓ Portable ZIP: releases/" -ForegroundColor Green
}

$installerFile = "releases/installer/FlashQuest-$Version.msi"
if (Test-Path $installerFile) {
    Write-Host "   ✓ Windows Installer: releases/installer/" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Windows Installer: Not created" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🚀 Ready to distribute! Check the releases/ directory for all build artifacts." -ForegroundColor Green
Write-Host ""