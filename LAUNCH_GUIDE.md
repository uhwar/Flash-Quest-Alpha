# FlashQuest Launch Guide 🚀

## Current Status: Phase 2A Complete! 🎉

FlashQuest now has a **fully functional user interface** with a beautiful dark theme and complete navigation system. Here's what you can do right now:

## 🎮 What's Working

### ✅ Complete Features
- **🏠 Main Menu**: Beautiful interface with live player stats
- **👤 Player Creation**: Create your hero on first launch
- **📊 Real-time Stats**: Level, XP progress, HP, achievements display
- **🗡️ Quest Selection**: Choose quests with player validation
- **📱 Navigation**: Smooth transitions between all screens
- **💾 Data Persistence**: All progress automatically saved
- **🎨 Professional UI**: Dark Obsidian-inspired theme throughout

### 🔄 In Development
- **Quest Gameplay Interface**: The actual flashcard quiz experience (coming next!)

## 🛠️ How to Run FlashQuest

### Option 1: With Maven (Recommended)
```bash
# Make sure you have Maven installed, then:
mvn clean compile
mvn javafx:run
```

### Option 2: Quick Test (Without JavaFX UI)
```bash
# Compile and test core functionality:
javac -cp "src/main/java" -d . src/main/java/com/flashquest/model/*.java src/main/java/com/flashquest/service/*.java TestUI.java
java TestUI
```

## 🎯 What You'll Experience

### First Launch
1. **Welcome Dialog** 🎮: Enter your player name to create your hero
2. **Main Menu** appears with your fresh Level 1 character
3. **32+ Java flashcards** automatically loaded and ready

### Main Menu Features
- **🚀 Start Quest**: Opens quest selection (currently leads to placeholder)
- **➕ Add Flashcard**: Shows "coming soon" message
- **📊 View Stats**: Shows placeholder for detailed statistics
- **📝 Manage Flashcards**: Shows placeholder for flashcard management

### Quest Selection
- **⚡ Quick Quest**: 10-question balanced quest (UI complete, gameplay coming next)
- **🛡️ Hero Status**: Shows your level, HP, and flashcard count
- **✅ Validation**: Won't let you start without proper setup

### Live Player Stats
- **⭐ Level & XP Progress**: Visual progress bar showing advancement
- **❤️ Health Points**: Current HP status
- **🏆 Achievements**: Shows unlocked title count
- **📈 Quest History**: Tracks completed and perfect quests

## 🐛 Known Limitations (Temporary)

- **Quest Gameplay**: Clicking "Start Quick Quest" goes to placeholder screen
- **Add Flashcard**: Shows info dialog (interface not yet built)
- **Detailed Stats**: Shows placeholder screen
- **Flashcard Manager**: Shows placeholder screen

## 🎨 Visual Design Highlights

- **Dark Theme**: Professional Obsidian-inspired color scheme
- **Modern Layout**: Clean, spacious interface with proper spacing
- **Game Elements**: HP bars, XP progress, achievement badges
- **Responsive Design**: Proper window sizing and scaling
- **Cute Emojis**: Throughout the interface for gamification ✨

## 📁 Save Data Location

Your game progress is automatically saved to:
- **Windows**: `%USERPROFILE%/flashquest-data/`
- **Mac/Linux**: `~/flashquest-data/`

Files include:
- `player.json` - Your character progress
- `flashcards.json` - Your flashcard collection  
- `quests.json` - Custom quests (future feature)
- Automatic backups with `.backup` extension

## 🔍 Troubleshooting

### JavaFX Issues
- Make sure you have Java 17+ installed
- Maven will automatically download JavaFX dependencies

### Missing Dependencies
- Run `mvn clean compile` first to ensure all dependencies are downloaded

### Save Data Issues
- Save data is created automatically in your home directory
- Check console output for any persistence errors

## 🚀 Next Steps

The **quest gameplay interface** is the final piece needed to make FlashQuest fully playable! Once that's complete, you'll be able to:

- Answer flashcard questions in a beautiful interface
- Use the honor system (Correct/Incorrect buttons)
- Earn XP and level up in real-time
- Track your learning progress
- Unlock achievements and titles

---

**Ready to see your creation come to life?** Run `mvn javafx:run` and experience FlashQuest! 🎮✨