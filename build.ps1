# FlashQuest Build Script (PowerShell)
# Handles Maven path automatically and builds the application

param(
    [switch]$SkipTests = $true,
    [switch]$Clean = $true,
    [string]$MavenPath = "C:\apache-maven\bin\mvn.cmd"
)

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "         FlashQuest Build Script" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Function to find Maven
function Find-Maven {
    # Check specified path first (the known working path)
    if (Test-Path $MavenPath) {
        Write-Host "✓ Maven found at: $MavenPath" -ForegroundColor Green
        return $MavenPath
    }
    
    # Search common locations
    $commonPaths = @(
        "C:\apache-maven\bin\mvn.cmd",
        "C:\Program Files\Apache\Maven\*\bin\mvn.cmd",
        "C:\tools\apache-maven\bin\mvn.cmd",
        "$env:M2_HOME\bin\mvn.cmd"
    )
    
    foreach ($path in $commonPaths) {
        if (Test-Path $path) {
            Write-Host "✓ Maven found at: $path" -ForegroundColor Green
            return $path
        }
        $resolved = Resolve-Path $path -ErrorAction SilentlyContinue
        if ($resolved) {
            Write-Host "✓ Maven found at: $resolved" -ForegroundColor Green
            return $resolved.Path
        }
    }
    
    # Check if mvn is in PATH as last resort
    try {
        $mvnCmd = Get-Command "mvn" -ErrorAction Stop
        Write-Host "✓ Maven found in PATH at: $($mvnCmd.Source)" -ForegroundColor Green
        return $mvnCmd.Source
    } catch {
        # Maven not found anywhere
    }
    
    return $null
}

# Find Maven
$mvnCommand = Find-Maven

if (-not $mvnCommand) {
    Write-Host "✗ ERROR: Maven not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "To fix this:" -ForegroundColor Yellow
    Write-Host "1. Install Maven from https://maven.apache.org/" -ForegroundColor Gray
    Write-Host "2. Extract to C:\apache-maven" -ForegroundColor Gray
    Write-Host "3. Or add Maven to your PATH environment variable" -ForegroundColor Gray
    Write-Host "4. Or specify custom path: .\build.ps1 -MavenPath 'C:\path\to\mvn.cmd'" -ForegroundColor Gray
    Write-Host ""
    exit 1
}

# Check Java
try {
    $javaVersion = & java -version 2>&1
    Write-Host "✓ Java is available" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 17 or later" -ForegroundColor Gray
    exit 1
}

Write-Host ""

# Build command
$buildArgs = @()
if ($Clean) {
    $buildArgs += "clean"
}
$buildArgs += "package"
if ($SkipTests) {
    $buildArgs += "-DskipTests=true"
}

Write-Host "Building FlashQuest (this may take a few minutes)..." -ForegroundColor Yellow
Write-Host "Command: $mvnCommand $($buildArgs -join ' ')" -ForegroundColor Gray
Write-Host ""

# Execute Maven build
try {
    Write-Host "Executing: $mvnCommand $($buildArgs -join ' ')" -ForegroundColor Gray
    
    # Use Start-Process for more reliable execution
    $process = Start-Process -FilePath $mvnCommand -ArgumentList $buildArgs -Wait -PassThru -NoNewWindow
    
    if ($process.ExitCode -ne 0) {
        throw "Maven build failed with exit code $($process.ExitCode)"
    }
} catch {
    Write-Host ""
    Write-Host "✗ ERROR: Build failed!" -ForegroundColor Red
    Write-Host "Check the output above for error details." -ForegroundColor Gray
    Write-Host "Debug info: mvnCommand = '$mvnCommand'" -ForegroundColor Gray
    Write-Host "Build args: $($buildArgs -join ' ')" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "✓ Build successful!" -ForegroundColor Green

# Copy JAR to releases folder
$targetJar = "target\FlashQuest-1.0.0.jar"
$releaseDir = "releases\standalone"

if (Test-Path $targetJar) {
    Write-Host "Copying JAR to releases folder..." -ForegroundColor Yellow
    
    # Ensure release directory exists
    if (-not (Test-Path $releaseDir)) {
        New-Item -ItemType Directory -Path $releaseDir -Force | Out-Null
    }
    
    Copy-Item $targetJar -Destination $releaseDir -Force
    Write-Host "✓ FlashQuest-1.0.0.jar copied to $releaseDir" -ForegroundColor Green
} else {
    Write-Host "WARNING: FlashQuest-1.0.0.jar not found in target directory" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "         Build Complete!" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Show created files
Write-Host "Files created:" -ForegroundColor White
if (Test-Path "$releaseDir\FlashQuest-1.0.0.jar") {
    Write-Host "   ✓ $releaseDir\FlashQuest-1.0.0.jar" -ForegroundColor Green
} else {
    Write-Host "   ✗ $releaseDir\FlashQuest-1.0.0.jar (MISSING)" -ForegroundColor Red
}

$otherFiles = @(
    "$releaseDir\FlashQuest.bat",
    "$releaseDir\README.txt"
)

foreach ($file in $otherFiles) {
    if (Test-Path $file) {
        Write-Host "   ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "   ! $file (not found, may need to be created)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "To run FlashQuest:" -ForegroundColor White
Write-Host "   1. Navigate to $releaseDir" -ForegroundColor Gray
Write-Host "   2. Double-click FlashQuest.bat" -ForegroundColor Gray
Write-Host "   3. Or run: java -jar FlashQuest-1.0.0.jar" -ForegroundColor Gray
Write-Host ""

# Offer to test immediately
$response = Read-Host "Test FlashQuest now? (y/N)"
if ($response -eq 'y' -or $response -eq 'Y') {
    Push-Location $releaseDir
    try {
        if (Test-Path "FlashQuest.bat") {
            .\FlashQuest.bat
        } else {
            java -jar FlashQuest-1.0.0.jar
        }
    } finally {
        Pop-Location
    }
}