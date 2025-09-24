# FlashQuest Build Instructions

## Prerequisites
1. **Java 17 or later** - Download from [Adoptium](https://adoptium.net/)
2. **Maven 3.6 or later** - Download from [Maven Apache](https://maven.apache.org/)
3. **JavaFX SDK** (optional, for development)

## Quick Build (Recommended)

### 1. Install Maven
- Download Maven from https://maven.apache.org/
- Extract to a folder (e.g., `C:\apache-maven-3.9.x`)
- Add `C:\apache-maven-3.9.x\bin` to your PATH environment variable
- Verify: Open new command prompt and run `mvn -version`

### 2. Build the Application
```batch
# Navigate to project directory
cd "C:\Code Directory\Flashcard-RPG-Project"

# Clean and build (skip tests for faster build)
mvn clean package -DskipTests=true

# Build with tests
mvn clean package
```

### 3. Create Distribution
```batch
# Copy JAR to releases folder
copy target\FlashQuest-1.0.0.jar releases\standalone\

# Test the distribution
cd releases\standalone
FlashQuest.bat
```

## Distribution Types

### 1. Standalone Distribution
- **Location**: `releases/standalone/`
- **Contents**: JAR file + launcher script
- **Usage**: Copy folder and run `FlashQuest.bat`

### 2. Portable Distribution (with Maven)
```batch
# Create ZIP with all dependencies
mvn assembly:single

# Result: releases/FlashQuest-1.0.0-portable.zip
```

### 3. Windows Installer (with jpackage)
```batch
# Requires JDK 14+ with jpackage
jpackage --input releases/standalone ^
         --name FlashQuest ^
         --main-jar FlashQuest-1.0.0.jar ^
         --main-class com.flashquest.FlashQuestApplication ^
         --type msi ^
         --dest releases/installer ^
         --win-dir-chooser ^
         --win-shortcut ^
         --win-menu
```

## Alternative Build Methods

### Without Maven (Manual)
If you can't install Maven, you can try manual compilation:

1. **Download JavaFX SDK** from https://openjfx.io/
2. **Set up classpath** with all JAR dependencies
3. **Compile Java sources** manually
4. **Create JAR** with proper manifest

*Note: This is complex and not recommended. Installing Maven is much easier.*

### Using IDE
- **IntelliJ IDEA**: Open project, run Maven goals in Maven tool window
- **Eclipse**: Import as Maven project, right-click → Run As → Maven build
- **VS Code**: Use Maven extension

## Project Structure
```
FlashQuest/
├── src/main/java/          # Java source code
├── src/main/resources/     # Resources (FXML, CSS, images)
├── src/test/java/          # Test code
├── releases/               # Distribution files
│   └── standalone/         # Standalone distribution
├── scripts/                # Build scripts
├── sample-data/            # Sample flashcard files
├── pom.xml                 # Maven configuration
└── README.md               # Project documentation
```

## Troubleshooting

### Maven Not Found
```batch
# Check if Maven is in PATH
mvn -version

# If not found, add Maven bin directory to PATH
# Windows: Add to System Environment Variables
```

### JavaFX Issues
```batch
# If JavaFX modules not found, try:
java --add-modules javafx.controls,javafx.fxml --module-path /path/to/javafx/lib -jar FlashQuest-1.0.0.jar
```

### Build Errors
```batch
# Clean everything and rebuild
mvn clean
mvn package -DskipTests=true -X  # -X for verbose output
```

## Development Setup

### Running from Source
```batch
# Using Maven JavaFX plugin
mvn javafx:run

# Or with Java directly (if JavaFX in classpath)
java -cp target/classes:target/dependency/* com.flashquest.FlashQuestApplication
```

### Testing
```batch
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=FlashcardTest
```

## Distribution Checklist

Before releasing:
- [ ] All tests pass: `mvn test`
- [ ] JAR builds successfully: `mvn package`
- [ ] Application starts: `java -jar target/FlashQuest-1.0.0.jar`
- [ ] Launcher script works: `releases/standalone/FlashQuest.bat`
- [ ] README.txt is up to date
- [ ] Sample data files included
- [ ] Version numbers are consistent

## Support
For build issues or questions:
- Check project documentation in README.md
- Review Maven logs for specific error messages
- Ensure Java and Maven versions meet requirements
- Verify all dependencies are available