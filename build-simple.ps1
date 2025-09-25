# FlashQuest Simple Build Script (PowerShell)
# Reliable build script that just works

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "         FlashQuest Build Script" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Maven path (known to work)
$MavenPath = "C:\apache-maven\bin\mvn.cmd"

# Check if Maven exists at known location
if (-not (Test-Path $MavenPath)) {
    Write-Host "ERROR: Maven not found at $MavenPath" -ForegroundColor Red
    Write-Host "Please ensure Maven is installed at C:\apache-maven" -ForegroundColor Yellow
    exit 1
}

Write-Host "Maven found at: $MavenPath" -ForegroundColor Green

# Check Java
try {
    $null = java -version 2>&1
    Write-Host "Java is available" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Building FlashQuest (this may take a few minutes)..." -ForegroundColor Yellow
Write-Host ""

# Execute Maven build directly
try {
    & $MavenPath clean package -DskipTests=true
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed"
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Build successful!" -ForegroundColor Green

# Check if JAR was created
$targetJar = "target\FlashQuest-1.0.0.jar"
$releaseDir = "releases\standalone"

if (Test-Path $targetJar) {
    Write-Host "JAR file created successfully: $targetJar" -ForegroundColor Green
    Write-Host "JAR is also copied to: $releaseDir\FlashQuest-1.0.0.jar" -ForegroundColor Green
} else {
    Write-Host "WARNING: JAR file not found at $targetJar" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "         Build Complete!" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "To run FlashQuest:" -ForegroundColor White
Write-Host "  1. Navigate to releases\standalone\" -ForegroundColor Gray
Write-Host "  2. Double-click FlashQuest.bat" -ForegroundColor Gray
Write-Host "  3. Or run: java -jar FlashQuest-1.0.0.jar" -ForegroundColor Gray
Write-Host ""

# Ask if user wants to test
$response = Read-Host "Test FlashQuest now? (y/N)"
if ($response -eq 'y' -or $response -eq 'Y') {
    if (Test-Path "$releaseDir\FlashQuest-1.0.0.jar") {
        Push-Location $releaseDir
        try {
            java -jar FlashQuest-1.0.0.jar
        } finally {
            Pop-Location
        }
    } else {
        Write-Host "Cannot test: JAR file not found in releases folder" -ForegroundColor Red
    }
}