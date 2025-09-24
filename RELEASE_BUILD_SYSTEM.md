# FlashQuest Release Build System

## Overview
A complete build system has been set up for creating distributable versions of FlashQuest. The system supports multiple distribution formats and automated build processes.

## What Was Created

### 1. Maven Build Configuration
- **Updated pom.xml** with build plugins:
  - Maven Shade Plugin (executable JAR)
  - Assembly Plugin (portable ZIP distribution)
  - JPackage Plugin (Windows installer)
  - Resource copying plugins

### 2. Release Directory Structure
```
releases/
├── installer/          # Windows MSI installers (when available)
├── portable/          # ZIP archives with dependencies
└── standalone/        # JAR + launcher script
    ├── FlashQuest.bat     # Windows launcher
    ├── README.txt         # User instructions
    └── (FlashQuest-1.0.0.jar) # To be built
```

### 3. Build Scripts
- **build.bat** - Simple Windows batch file for basic builds
- **build-releases.ps1** - Full PowerShell script (complex, needs debugging)
- **simple-build.ps1** - Basic PowerShell script for directory setup

### 4. Assembly Configuration
- **src/assembly/portable.xml** - Maven assembly descriptor for ZIP distribution
- Includes JAR, documentation, sample data, and launcher scripts

### 5. Support Files
- **scripts/FlashQuest.bat** - Template launcher script
- **src/main/resources/jvm.args** - JavaFX runtime arguments
- **src/main/resources/icon.ico** - Placeholder for application icon

### 6. Documentation
- **BUILD_INSTRUCTIONS.md** - Comprehensive build guide
- **RELEASE_BUILD_SYSTEM.md** - This summary document
- **releases/standalone/README.txt** - End-user instructions

## How to Build

### Quick Start (Recommended)
1. Install Maven from https://maven.apache.org/
2. Add Maven to your PATH environment variable  
3. Run `build.bat` from project root
4. Find distribution in `releases/standalone/`

### Manual Build
```batch
mvn clean package -DskipTests=true
copy target\FlashQuest-1.0.0.jar releases\standalone\
```

### Full Distribution Build (when Maven is working)
```batch
mvn clean package assembly:single -DskipTests=true
```

## Distribution Types

### 1. Standalone (✓ Ready)
- **Location**: `releases/standalone/`
- **Contents**: 
  - FlashQuest-1.0.0.jar (executable JAR)
  - FlashQuest.bat (Windows launcher)
  - README.txt (instructions)
- **Usage**: Copy folder, run FlashQuest.bat
- **Size**: ~5-10 MB (depending on dependencies)

### 2. Portable (🔧 Configured)
- **Location**: `releases/FlashQuest-1.0.0-portable.zip`
- **Contents**: Standalone + sample data + documentation
- **Usage**: Extract ZIP, run FlashQuest.bat
- **Size**: ~10-15 MB (includes samples)

### 3. Windows Installer (🔧 Optional)
- **Location**: `releases/installer/FlashQuest-1.0.0.msi`
- **Requirements**: JDK 14+ with jpackage
- **Usage**: Double-click MSI to install
- **Features**: Start menu shortcut, Add/Remove Programs entry

## Current Status

### ✅ Completed
- Maven pom.xml configuration with all necessary plugins
- Release directory structure created
- Standalone launcher script (FlashQuest.bat)
- User documentation (README.txt)
- Build instructions and system documentation
- Assembly descriptor for portable distribution

### 🔧 Needs Maven
- Actual JAR compilation (requires `mvn package`)
- Portable ZIP creation (requires `mvn assembly:single`)
- Windows installer creation (requires `jpackage`)

### 🎯 Ready to Use
Once Maven is installed and the JAR is built:
1. The `releases/standalone/` folder can be distributed as-is
2. Users can run FlashQuest.bat to start the game
3. All necessary documentation is included

## Technical Details

### Maven Plugins Used
- **maven-shade-plugin**: Creates fat JAR with all dependencies
- **maven-assembly-plugin**: Creates ZIP distribution with extras
- **jpackage-maven-plugin**: Creates native installers
- **maven-resources-plugin**: Copies JARs to release directories

### JavaFX Configuration
- Shade plugin includes JavaFX modules automatically
- Launcher scripts use `--add-modules javafx.controls,javafx.fxml`
- Runtime arguments configured in src/main/resources/jvm.args

### File Structure
```
FlashQuest-1.0.0/           # Portable distribution
├── FlashQuest.jar          # Renamed main JAR
├── FlashQuest.bat          # Launcher script
├── README.txt              # User instructions
├── sample-data/            # Example flashcards
│   └── basic-math.json
├── config/                 # Configuration examples
└── scripts/                # Additional utilities
```

## Next Steps

To complete the build system:
1. **Install Maven** on the development machine
2. **Test build process**: Run `build.bat` to verify everything works
3. **Create installer**: Use jpackage for Windows MSI (optional)
4. **Test distributions**: Verify all three distribution types work correctly
5. **Automate releases**: Set up CI/CD pipeline (GitHub Actions, etc.)

## Troubleshooting

### Maven Issues
- **Not found**: Install Maven, add to PATH
- **Build fails**: Check Java version (requires Java 17+)
- **Missing dependencies**: Run `mvn dependency:resolve`

### JavaFX Issues  
- **Module not found**: Ensure JavaFX is included in runtime
- **Display problems**: Check graphics drivers and Java version

### Distribution Issues
- **JAR won't run**: Verify Java 17+ is installed on target machine
- **Missing files**: Ensure all dependencies are included in JAR

## Resources
- [Maven Download](https://maven.apache.org/download.cgi)
- [Java Download (Adoptium)](https://adoptium.net/)
- [JavaFX Documentation](https://openjfx.io/)
- [jpackage Documentation](https://docs.oracle.com/en/java/javase/17/jpackage/)