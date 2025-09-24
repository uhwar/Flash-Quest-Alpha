# FlashQuest - Educational RPG

FlashQuest is a gamified flashcard learning application that combines educational content with RPG-style progression mechanics. Learn while you play!

## 🎮 Features

### Core Gameplay
- **RPG Progression**: Level up your character by answering questions correctly
- **Health & XP System**: Manage your health while earning experience points
- **Difficulty Scaling**: Questions adapt to your skill level
- **Multiple Categories**: Support for various subject areas

### Learning System
- **Import Flashcards**: Load your own flashcard sets from JSON files
- **Difficulty Levels**: Easy, Medium, Hard, and Expert questions
- **Progress Tracking**: Monitor your learning journey
- **Save Profiles**: Multiple save files for different players or subjects

### Technical Features
- **Database Integration**: H2 embedded database for data persistence
- **Modern UI**: JavaFX-based interface with FXML layouts
- **Cross-Platform**: Runs on Windows, Mac, and Linux

## 🚀 Quick Start

### Requirements
- Java 17 or later
- No additional dependencies required (all bundled)

### Running FlashQuest
1. Download the latest release
2. Extract the ZIP file
3. Double-click `FlashQuest.bat` (Windows) or run the JAR directly
4. Create your player profile and start learning!

### Manual Launch
```bash
java -jar FlashQuest-1.0.0.jar
```

## 📚 Using Flashcards

### Importing Custom Flashcards
FlashQuest supports JSON-formatted flashcard files. Example format:

```json
{
  "cards": [
    {
      "question": "What is the capital of France?",
      "answer": "Paris",
      "difficulty": "EASY",
      "category": "Geography"
    },
    {
      "question": "Calculate: 15 × 23",
      "answer": "345", 
      "difficulty": "MEDIUM",
      "category": "Mathematics"
    }
  ]
}
```

### Supported Difficulty Levels
- **EASY**: Basic questions, +10 XP, -5 HP on wrong answer
- **MEDIUM**: Intermediate questions, +20 XP, -10 HP on wrong answer  
- **HARD**: Advanced questions, +30 XP, -15 HP on wrong answer
- **EXPERT**: Expert level, +50 XP, -20 HP on wrong answer

### Categories
Organize your flashcards into categories like:
- Mathematics
- Science
- History
- Geography
- Languages
- And more!

## 🎯 Game Mechanics

### Character Progression
- Start at Level 1 with 100 HP
- Gain XP by answering questions correctly
- Level up when you reach the XP threshold
- Higher levels unlock harder questions

### Health System
- Start with 100 HP
- Lose health for wrong answers (varies by difficulty)
- Game over when health reaches 0
- Health can be restored through items or level-ups

### Save System
- Multiple save profiles supported
- Automatic progress saving
- Profile management interface
- Player statistics tracking

## 🛠️ Development

### Building from Source
```bash
# Clone the repository
git clone https://github.com/flashquest/flashquest-rpg.git

# Build with Maven
mvn clean package -DskipTests=true

# Run the application
java -jar target/FlashQuest-1.0.0.jar
```

### Project Structure
```
FlashQuest/
├── src/main/java/          # Application source code
├── src/main/resources/     # FXML layouts, CSS, assets
├── src/test/java/          # Unit tests
├── releases/               # Distribution files
├── sample-data/            # Example flashcard files
└── pom.xml                # Maven configuration
```

### Architecture
- **Model**: Player, Flashcard, Game state management
- **View**: JavaFX FXML layouts and controllers
- **Database**: H2 embedded database for persistence
- **Services**: Import/export, game logic, data access

## 📦 Distribution

### Available Formats
- **Standalone**: JAR + launcher script
- **Portable**: ZIP with sample data and documentation
- **Installer**: Windows MSI (when available)

### System Requirements
- **Java**: Version 17 or later
- **Memory**: 512 MB RAM minimum
- **Storage**: 50 MB available space
- **OS**: Windows 10+, macOS 10.14+, Linux

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md] for guidelines.

### Areas for Contribution
- New game mechanics and features
- Additional flashcard import formats
- UI/UX improvements
- Performance optimizations
- Bug fixes and testing

## 📄 License

This project is licensed under the MIT License - see the [LICENSE] file for details.

## 🔗 Links

- **Documentation**: [Wiki](https://github.com/flashquest/flashquest-rpg/wiki)
- **Bug Reports**: [Issues](https://github.com/flashquest/flashquest-rpg/issues)
- **Discussions**: [GitHub Discussions](https://github.com/flashquest/flashquest-rpg/discussions)

## 🙏 Acknowledgments

- JavaFX community for UI framework
- Jackson library for JSON processing
- H2 Database for embedded data storage
- All contributors and testers

---

**Happy Learning with FlashQuest!** 🎓✨