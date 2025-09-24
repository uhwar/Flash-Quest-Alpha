# FlashQuest Agent Rule Set 🎮✨

## Project Overview
**FlashQuest** is a gamified flashcard learning application that combines RPG progression mechanics with educational content. Players embark on quests, answer questions, gain experience, and progress through levels while learning Java programming concepts (or custom topics).

---

## 🎯 Core Game Mechanics

### Player Progression System
- **Experience Points (XP)**: Primary progression currency
- **Levels**: Incremental XP requirements using exponential curve
- **Health Points (HP)**: Failure mitigation system with permadeath mechanics
- **Quests**: Structured learning sessions of 10 questions each
- **Starting Stats**: Players begin with 3 HP at Level 1

### XP Calculation Formula
```
Level N XP Requirement = 100 + (N-1) * 150 + ((N-1)^2 * 25)
```
**Progression Curve**: 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700, 3250...

### Quest Structure
- **Length**: Exactly 10 questions per quest (configurable in Quest Designer: 5-20)
- **XP Reward**: Base 50 XP per completed quest + bonus modifiers
- **Question Selection**: Weighted random from available flashcard pool
- **Honor System**: Player self-evaluates correctness (Correct/Incorrect buttons)

**XP Distribution**:
- Base Quest Completion: 50 XP
- Correct Answer Bonus: +10 XP per correct answer
- Perfect Quest Bonus: +25 XP (all 10 correct)
- **Maximum XP per quest**: 175 XP (50 + 100 + 25)

### Difficulty Scaling Bonuses
- **Easy Questions**: Standard XP rewards
- **Medium Questions**: +5 XP bonus per correct answer
- **Hard Questions**: +10 XP bonus per correct answer

---

## ❤️ Health Point (HP) System

### HP Mechanics
- **Starting HP**: 3 HP at quest start (customizable: 50-200 in Quest Designer)
- **HP Loss**: -1 HP per incorrect answer
- **HP Recovery**: None during quest (permadeath within quest)
- **Death Condition**: HP ≤ 0 = Quest failure, return to main menu
- **HP Reset**: Full HP restoration between quests

---

## 🃏 Flashcard Management System

### Flashcard Structure
```java
class Flashcard {
    String question;
    String answer;
    String category;
    DifficultyLevel difficulty; // Easy, Medium, Hard
    LocalDateTime dateCreated;
    int timesAsked;
    int timesCorrect;
}
```

### Default Java Flashcard Categories
- **Java Basics**: Syntax, variables, data types
- **Object-Oriented Programming**: Classes, inheritance, polymorphism
- **Collections Framework**: Lists, Maps, Sets, Iterators
- **Exception Handling**: Try-catch, custom exceptions
- **Concurrency**: Threads, synchronization, executors
- **Advanced Topics**: Generics, annotations, lambda expressions

### Sample Java Flashcards
1. **Q**: "What is the difference between `==` and `.equals()` in Java?"  
   **A**: "`==` compares object references, `.equals()` compares object content"

2. **Q**: "What access modifier makes a member accessible only within the same package?"  
   **A**: "Package-private (no modifier) or `protected`"

3. **Q**: "Which collection allows duplicate elements and maintains insertion order?"  
   **A**: "ArrayList or LinkedList"

---

## 🎨 User Interface Specifications

### Design Theme
- **Dark and clean aesthetic** like Obsidian note app
- **Sleek, modern design** with professional appearance
- **Cute emojis** throughout to enhance gamification
- **Gilded achievement boards** for prestige display

### Main Menu Components
- **Start Quest Button** 🚀: Opens quest selection screen
- **Add Flashcard Button** ➕: Opens flashcard creation form
- **View Stats Button** 📊: Display player level, XP, and statistics
- **Manage Flashcards Button** 📝: Edit/delete existing flashcards
- **Disable Base Flashcards** ❌: Toggle to focus on custom material only

### Quest Selection Screen
- **Available Quests Display**: List of created quests with metadata
- **Quick Start Option**: Use default 10-question random quest
- **Quest Designer Access**: Create new custom quests

### Quest Interface Components
- **Question Display**: Large, readable text area
- **Answer Reveal Button** 👁️: Shows answer when clicked
- **Self-Assessment Buttons**: "Correct" ✅ (green) and "Incorrect" ❌ (red)
- **Progress Indicators**:
  - Question counter (e.g., "Question 3/10")
  - HP bar with visual indicator ❤️
  - Current XP and level display ⭐

### Flashcard Management Interface
- **Add New Flashcard Form**:
  - Question text area (required)
  - Answer text area (required)
  - Category dropdown (default categories + custom)
  - Difficulty selector (Easy/Medium/Hard)
- **Flashcard List View**:
  - Search/filter functionality 🔍
  - Edit and delete buttons per card
  - Statistics per card (times asked, accuracy rate)

---

## 🎯 Quest Designer Interface

### Basic Quest Parameters
- **Quest Name**: Required, 50 character limit, must be unique per player
- **Quest Description**: Optional, 200 character limit for quest purpose/theme
- **Question Count**: Configurable from 5-20 questions (default: 10)
- **Difficulty Focus**: Easy/Medium/Hard or custom distribution percentages
- **HP Override**: Custom starting HP (50-200, default: 100)
- **Category Filter**: Select specific flashcard categories to include/exclude

---

## 🏆 Achievement & Title System

### Achievement Titles (Display with gilded boards)
- **Scholar** 📚: Complete 10 quests
- **Perfectionist** 💎: Complete 5 perfect quests (no wrong answers)
- **Survivor** 🛡️: Complete a quest with exactly 1 HP remaining
- **Knowledge Seeker** 🔍: Create 50 custom flashcards
- **Java Master** ☕: Answer 100 Java-related questions correctly

### Title Display
- Titles appear prominently in UI during gameplay
- **Gilded boards** for prestige and visual appeal
- Current active title displayed on main menu and quest screens

---

## 📊 Game Balance & Pacing

### Progression Pacing
- **Early Levels (1-5)**: Achievable with 2-3 perfect quests per level
- **Mid Levels (6-15)**: Requires 4-6 quests per level
- **High Levels (16+)**: Requires 8+ quests per level

### Question Selection Algorithm
**Weighted Random** based on:
- Questions answered less frequently get higher weight
- Category distribution ensures variety within each quest
- Difficulty balance according to quest settings

---

## 💾 Data Storage Requirements

### Required Storage Components
- **Player Profile**: Name, current level, total XP, quests completed, active title
- **Flashcard Database**: All user-created and default flashcards with metadata
- **Statistics**: Per-flashcard performance metrics, quest history
- **Achievement Progress**: Unlockable goals and milestone tracking
- **Game Save**: Persistent player data between sessions

### Player Initialization
- **Player Name Selection**: Required at first launch, saved to game save
- **Welcome Tutorial**: Guide through first flashcard creation and quest

---

## 🛡️ Honor System Implementation

### Trust-Based Assessment
- No automatic answer validation against stored answers
- Player clicks "Correct" ✅ or "Incorrect" ❌ after viewing answer
- Encourages honest self-assessment for genuine learning
- Optional: Track self-assessment patterns for insights (future feature)

---

## ⚠️ Error Handling & Edge Cases

### Critical Error Scenarios
- **Empty Flashcard Pool**: Show tutorial to create first flashcard
- **Quest Interruption**: Save progress, allow resume on restart
- **Data Corruption**: Backup and restore mechanisms
- **Invalid Quest Parameters**: Validation with helpful error messages

### User Experience Safeguards
- **Confirmation Dialogs**: For destructive actions (delete flashcard/quest)
- **Input Validation**: Prevent empty required fields, invalid characters
- **Progress Preservation**: Auto-save during quests at question intervals

---

## 🚀 Development Priorities

### Phase 1: Core Mechanics
1. Player progression system (XP, levels, HP)
2. Basic flashcard CRUD operations
3. Quest execution engine
4. Honor system self-assessment

### Phase 2: User Interface
1. Dark theme implementation
2. Main menu and navigation
3. Quest interface with progress indicators
4. Flashcard management screens

### Phase 3: Advanced Features
1. Quest Designer interface
2. Achievement system with titles
3. Statistics and analytics
4. Data persistence and save system

### Phase 4: Polish & Enhancement
1. Animation and visual effects
2. Sound effects and feedback
3. Tutorial system
4. Performance optimization

---

## 📝 Technical Implementation Notes

### Technology Stack
- **Language**: Java (primary focus for educational content)
- **Framework**: To be determined based on requirements
- **Data Storage**: Local file system or lightweight database
- **UI Framework**: Modern, responsive design supporting dark themes

### Code Organization
- Clean separation of game logic and UI components
- Modular design for easy feature additions
- Comprehensive error handling and logging
- Unit tests for core game mechanics

---

*This document serves as the authoritative guide for FlashQuest development. All features and mechanics should align with these specifications to ensure consistent gameplay experience and educational effectiveness.* ✨🎮📚