# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

**FlashQuest** is a gamified flashcard learning application that combines RPG progression mechanics with educational content. Built with Java 17, JavaFX, and Maven, it provides an immersive learning experience where players embark on quests, answer questions, gain XP, and progress through levels while mastering Java programming concepts or custom topics.

## Common Development Commands

### Building the Application
```bash
# Clean and build (skip tests for faster build)
mvn clean package -DskipTests=true

# Full build with tests
mvn clean package

# Compile only (no packaging)
mvn compile
```

### Testing Commands
```bash
# Run tests only (without rebuilding if sources haven't changed)
mvn test

# Run specific test class
mvn test -Dtest=FlashcardTest

# Run specific test method
mvn test -Dtest=FlashcardTest#testCreateFlashcard

# Run tests with pattern matching
mvn test -Dtest="*Test"

# Run tests in specific package
mvn test -Dtest="com.flashquest.service.*Test"

# Skip compilation and just run tests (if already compiled)
mvn surefire:test

# Clean compile and test (when you need fresh build)
mvn clean compile test
```

### Running the Application
```bash
# Run from source using JavaFX plugin
mvn javafx:run

# Run compiled JAR directly
java -jar target/FlashQuest-1.0.0.jar

# Use launcher (handles JavaFX module issues)
java -jar target/FlashQuest-1.0.0.jar

# Run with debug mode
java -Dlogback.configurationFile=logback-debug.xml -jar target/FlashQuest-1.0.0.jar
```

### Distribution and Packaging
```bash
# Create standalone JAR
mvn clean package

# Create portable ZIP distribution
mvn assembly:single

# Copy JAR to releases folder
copy target\FlashQuest-1.0.0.jar releases\standalone\

# Test standalone distribution
cd releases/standalone && ./FlashQuest.bat
```

### Development Workflow
```bash
# Quick development cycle (compile + run)
mvn compile javafx:run

# Efficient testing workflow (compile only when needed)
mvn compile && mvn test

# Test specific areas without full rebuild
mvn test -Dtest="*ServiceTest"

# Quick test after small changes (skip compilation if not needed)
mvn surefire:test

# Clean everything and rebuild from scratch (only when necessary)
mvn clean compile
```

## Architecture Overview

### Core Application Structure

**FlashQuest** follows a layered architecture with clear separation of concerns:

#### Entry Points
- **`Launcher.java`**: Main entry point that handles JavaFX module system issues and universal scaling configuration
- **`FlashQuestApplication.java`**: JavaFX Application class that sets up the primary window and initializes the game

#### UI Layer (`com.flashquest.ui`)
- **`AppController.java`**: Central controller managing screen navigation, window scaling, and app lifecycle
- **FXML Controllers**: Screen-specific controllers for each game view (MainMenu, Quest, FlashcardManager, etc.)
- **Universal Scaling System**: Adaptive UI scaling for different screen resolutions and DPI settings

#### Service Layer (`com.flashquest.service`)
- **`GameService.java`**: Main game logic coordinator and singleton service
- **`DataService.java`**: JSON-based data persistence with backup/restore capabilities  
- **`DefaultFlashcardService.java`**: Manages default Java programming flashcard sets
- **`FlashcardImportService.java`**: Handles importing flashcards from text files with validation

#### Model Layer (`com.flashquest.model`)
- **`Player.java`**: Player progression, stats, and achievement tracking
- **`Flashcard.java`**: Individual flashcard with learning statistics and selection weighting
- **`Quest.java`**: Quest management, question selection, and XP calculation
- **`DifficultyLevel.java`**: Enum for Easy/Medium/Hard difficulty scaling

#### Data Flow Architecture
1. **UI Controllers** → **GameService** (single point of coordination)
2. **GameService** → **DataService** (persistence operations)
3. **GameService** → **Model Objects** (business logic)
4. **Models** → **DataService** (automatic JSON serialization)

### Key Architectural Patterns

- **Singleton Pattern**: GameService ensures single game state
- **MVC Pattern**: Clear separation between UI (FXML), Controllers, and Models
- **Service Layer**: Business logic abstracted from UI concerns
- **Data Transfer**: Jackson JSON serialization with backup mechanisms

## FlashQuest Game Rules and Mechanics

### Player Progression System

- **Experience Points (XP)**: Primary progression currency
- **Levels**: Incremental XP requirements (Level 1: 100 XP, Level 2: 250 XP, Level 3: 450 XP, etc.)
- **Health Points (HP)**: Failure mitigation system with permadeath mechanics
- **Quests**: Structured learning sessions of 10 questions each
- Player should start with 3 HP at their first level

### XP Calculation Formula

```
Level N XP Requirement = 100 + (N-1) * 150 + ((N-1)^2 * 25)
```

This creates an exponential curve: 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700, 3250...

### Quest Structure

- **Length**: Exactly 10 questions per quest
- **XP Reward**: Base 50 XP per completed quest + bonus modifiers
- **Question Selection**: Random from available flashcard pool
- **Honor System**: Player self-evaluates correctness (Correct/Incorrect buttons)

**XP Distribution**:

- Base Quest Completion: 50 XP
- Correct Answer Bonus: +10 XP per correct answer
- Perfect Quest Bonus: +25 XP (all 10 correct)
- Maximum XP per quest: 175 XP (50 + 100 + 25)

### Health Point (HP) System

#### HP Mechanics

- **Starting HP**: 3 HP at quest start
- **HP Loss**: -1 HP per incorrect answer
- **HP Recovery**: None during quest (permadeath within quest)
- **Death Condition**: HP ≤ 0 = Quest failure, return to main menu
- **HP Reset**: Full HP restoration between quests

### Flashcard Management System

#### Flashcard Structure

```java
class Flashcard {
    String question;
    String answer;
    String category;
    DifficultyLevel difficulty;
    LocalDateTime dateCreated;
    int timesAsked;
    int timesCorrect;
}
```

#### Default Java Flashcard Categories

- **Java Basics**: Syntax, variables, data types
- **Object-Oriented Programming**: Classes, inheritance, polymorphism
- **Collections Framework**: Lists, Maps, Sets, Iterators
- **Exception Handling**: Try-catch, custom exceptions
- **Concurrency**: Threads, synchronization, executors
- **Advanced Topics**: Generics, annotations, lambda expressions

#### Sample Java Flashcards

1. **Q**: "What is the difference between `==` and `.equals()` in Java?" **A**: "`==` compares object references, `.equals()` compares object content"
2. **Q**: "What access modifier makes a member accessible only within the same package?" **A**: "Package-private (no modifier) or `protected`"
3. **Q**: "Which collection allows duplicate elements and maintains insertion order?" **A**: "ArrayList or LinkedList"

### Main Menu Components

- **Start Quest Button**: Initiates new 10-question quest
- **Add Flashcard Button**: Opens flashcard creation form
- **View Stats Button**: Display player level, XP, and statistics
- **Manage Flashcards Button**: Edit/delete existing flashcards
- **Disable Base Flashcards**: Disables the base flashcard set so the player can use focus material

### Quest Interface Components

- **Question Display**: Large, readable text area
- **Answer Reveal Button**: Shows answer when clicked
- **Self-Assessment Buttons**: "Correct" (green) and "Incorrect" (red)
- **Progress Indicators**:
    - Question counter (e.g., "Question 3/10")
    - HP bar with visual indicator
    - Current XP and level display

### Flashcard Management Interface

- **Add New Flashcard Form**:
    - Question text area (required)
    - Answer text area (required)
    - Category dropdown (default categories + custom)
    - Difficulty selector (Easy/Medium/Hard)
- **Flashcard List View**:
    - Search/filter functionality
    - Edit and delete buttons per card
    - Statistics per card (times asked, accuracy rate)

### Game Balance Rules

#### Difficulty Scaling

- **Easy Questions**: Standard XP rewards
- **Medium Questions**: +5 XP bonus per correct answer
- **Hard Questions**: +10 XP bonus per correct answer

#### Progression Pacing

- **Early Levels (1-5)**: Achievable with 2-3 perfect quests per level
- **Mid Levels (6-15)**: Requires 4-6 quests per level
- **High Levels (16+)**: Requires 8+ quests per level

#### Required Storage

- **Player Profile**: Current level, total XP, quests completed
- **Flashcard Database**: All user-created and default flashcards
- **Statistics**: Per-flashcard performance metrics
- **Achievement Progress**: Unlockable goals and milestones

### Achievement Title System (Optional Extension)

- **Below the titles should appear somewhere in the UI for the player white fighting but with a gilded board for prestige**
- **Scholar**: Complete 10 quests
- **Perfectionist**: Complete 5 perfect quests (no wrong answers)
- **Survivor**: Complete a quest with exactly 1 HP remaining
- **Knowledge Seeker**: Create 50 custom flashcards
- **Java Master**: Answer 100 Java-related questions correctly

### Honor System Implementation

- No answer validation against stored answers
- Player clicks "Correct" or "Incorrect" after viewing answer
- Trust-based system encourages honest self-assessment
- Optional: Track self-assessment patterns for insights

### Randomization Rules

- **Question Selection**: Weighted random based on:
    - Questions answered less frequently get higher weight
    - Category distribution (ensure variety within quest)

#### Important General Notes
- Player should choose a player name at launch which should be saved to a game save.
- Game overall design should be very sleek. I want the theme to be dark and clean like obsidian note app.
- Cute emojis are more then welcome in designing the app and making it feel more gamified.
- Make sure we create a quest selection screen when creating the play button and overall systems.

### Error Handling

- **Empty Flashcard Pool**: Show tutorial to create first flashcard
- **Quest Interruption**: Save progress, allow resume on restart
- **Data Corruption**: Backup and restore mechanisms

### Quest Designer Interface

#### Basic Quest Parameters

- **Quest Name**: Required, 50 character limit, must be unique per player
- **Quest Description**: Optional, 200 character limit for quest purpose/theme
- **Question Count**: Configurable from 5-20 questions (default: 10)
- **Difficulty Focus**: Easy/Mid/Hard or custom distribution
- **HP Override**: Custom starting HP (50-200, default: 100)

## Important Development Guidelines

### Code Organization
- **Package Structure**: Follow `com.flashquest.{layer}.{feature}` convention
- **Service Layer**: All business logic should go through GameService as the coordinator
- **Data Persistence**: Use DataService for all save/load operations with automatic JSON backup
- **UI Controllers**: Keep controllers thin, delegate to services for business logic

### Universal Scaling Support
- **Resolution Support**: 4K (1.4x scale), 1440p (1.2x scale), 1080p (1.0x scale), lower (0.8-0.9x scale)
- **DPI Awareness**: Automatic detection and scaling configuration in Launcher.java
- **CSS Classes**: Use scale-small, scale-medium, scale-large, scale-xlarge for different scaling levels

### Error Handling Standards
- **Logging**: Use SLF4J logger in all classes for consistent logging
- **User Feedback**: Always provide meaningful error messages to users
- **Data Safety**: All data operations should have backup and recovery mechanisms
- **Validation**: Validate all user inputs before processing

### Testing Approach
- **Unit Tests**: Focus on model classes and service layer logic
- **Integration Tests**: Test data persistence and game flow
- **Manual Testing**: UI and user experience validation
- **Test Data**: Use sample flashcards for consistent testing

### Efficient Testing Workflow
- **Use `mvn test`** for incremental testing (only recompiles changed sources)
- **Use `mvn surefire:test`** to run tests without any compilation (fastest)
- **Use `mvn test -Dtest=ClassName`** to run specific test classes
- **Use `mvn test -Dtest=ClassName#methodName`** for individual test methods
- **Only use `mvn clean`** when you need to force a complete rebuild
- **Pattern matching**: `mvn test -Dtest="*Service*"` to test service classes only

## File Structure

```
src/main/java/com/flashquest/
├── Launcher.java                    # Entry point with JavaFX module handling
├── FlashQuestApplication.java       # JavaFX Application class
├── model/                          # Data models and business objects
│   ├── Player.java                 # Player progression and stats
│   ├── Flashcard.java             # Individual flashcard with statistics
│   ├── Quest.java                 # Quest management and XP calculation
│   └── DifficultyLevel.java       # Difficulty scaling enum
├── service/                        # Business logic and coordination
│   ├── GameService.java           # Main game coordinator (singleton)
│   ├── DataService.java           # JSON persistence with backups
│   ├── DefaultFlashcardService.java # Java programming flashcard sets
│   └── FlashcardImportService.java  # Text file import with validation
└── ui/                            # User interface layer
    ├── AppController.java         # Main navigation and scaling controller
    └── [FXML Controllers]         # Screen-specific controllers

src/main/resources/
├── fxml/                          # JavaFX FXML layouts
├── styles/                        # CSS stylesheets (dark theme)
└── assets/                        # Images, icons, and other resources
```

## Data Storage

- **Location**: `~/flashquest-data/` directory
- **Format**: JSON files with automatic backup (.backup suffix)
- **Files**: player.json, flashcards.json, quests.json
- **Backup Strategy**: Copy-on-write with automatic corruption recovery