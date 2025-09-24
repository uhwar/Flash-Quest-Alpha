# 🚀 FlashQuest Launch Instructions

## **Ready to Play FlashQuest!** 🎮

Your complete educational RPG is ready to launch! Follow these steps to start your Java learning adventure.

---

## 🛠️ **Prerequisites**

### **Required Software:**
1. **Java 17+** ✅ (You have Java 24 - Perfect!)
2. **Maven 3.6+** (Download from: https://maven.apache.org/download.cgi)

### **Quick Maven Installation (Windows):**
1. Download Maven from Apache Maven website
2. Extract to `C:\apache-maven-3.x.x`
3. Add `C:\apache-maven-3.x.x\bin` to your PATH environment variable
4. Restart PowerShell
5. Test with: `mvn --version`

---

## 🚀 **Launch Commands**

### **Option 1: Full JavaFX Launch (Recommended)**
```bash
# In your project directory:
mvn clean compile
mvn javafx:run
```

### **Option 2: Alternative Launch Method**
```bash
# If JavaFX plugin has issues:
mvn clean compile exec:java -Dexec.mainClass="com.flashquest.FlashQuestApplication"
```

### **Option 3: Create Executable JAR**
```bash
# Build standalone executable:
mvn clean package
java -jar target/flashquest-rpg-1.0.0-shaded.jar
```

---

## 🎮 **What to Expect**

### **First Launch Experience:**
1. **Welcome Dialog** 🎭: Enter your hero's name
2. **Main Menu Loads** 🏠: See your Level 1 character with 0 XP
3. **32 Java Flashcards** 📚: Automatically loaded and ready
4. **Beautiful Dark Theme** 🌙: Obsidian-inspired professional UI

### **Game Flow:**
1. **🏠 Main Menu** → View stats, navigate options
2. **🗡️ Start Quest** → Choose Quick Quest (10 questions)
3. **📋 Quest Selection** → Validate readiness, see hero status  
4. **🎮 Quest Gameplay** → Answer questions, earn XP, level up!
5. **🏆 Quest Complete** → See results, achievements, return to menu

---

## 🎯 **Gameplay Guide**

### **Quest Mechanics:**
- **❤️ Health Points**: Start with 3 HP, lose 1 for wrong answers
- **⭐ Experience**: Earn 10 XP per correct answer + difficulty bonuses
- **🎯 Perfect Bonus**: +25 XP for answering all questions correctly
- **📈 Leveling**: Automatic level-ups with exponential XP curve

### **Self-Assessment System:**
1. Read the question carefully
2. Click **"👁️ Show Answer"** when ready
3. Study the provided answer
4. Honestly assess: **"✅ Correct"** or **"❌ Incorrect"**
5. Earn XP and continue to next question

### **Achievement Unlocks:**
- **📚 Scholar**: Complete 10 quests
- **💎 Perfectionist**: Complete 5 perfect quests  
- **🛡️ Survivor**: Complete quest with exactly 1 HP
- **☕ Java Master**: Answer 100 Java questions correctly

---

## 🐛 **Troubleshooting**

### **Maven Not Found:**
```bash
# Check if Maven is installed:
mvn --version

# If not found, install Maven and add to PATH
# Then restart your terminal
```

### **JavaFX Module Issues:**
```bash
# Try with explicit module path:
mvn clean compile
mvn javafx:run -Djavafx.runtime.path="C:\Program Files\Java\javafx-sdk-21\lib"
```

### **Compilation Errors:**
```bash
# Clean and rebuild:
mvn clean
mvn compile
mvn javafx:run
```

### **Save Data Issues:**
- Game data saves to: `%USERPROFILE%\flashquest-data\`
- Delete this folder to reset game progress if needed

---

## 🎨 **UI Features to Explore**

### **Main Menu:**
- **Live Player Stats**: Level, XP progress bar, HP, achievements
- **Quest History**: Track completed and perfect quests
- **Navigation Buttons**: All lead to functional screens

### **Quest Interface:**
- **Progress Indicators**: Question counter, quest progress bar
- **Real-time Updates**: HP bar, XP earned, player level
- **Beautiful Animations**: Fade effects, smooth transitions
- **Professional Layout**: Clean, readable, engaging design

---

## 🎓 **Educational Content**

### **32+ Java Programming Questions:**
- **Java Basics**: Syntax, data types, operators, variables
- **OOP Concepts**: Classes, inheritance, polymorphism, encapsulation
- **Collections**: ArrayList, HashMap, iterators, comparators  
- **Exception Handling**: Try-catch, custom exceptions, best practices
- **Concurrency**: Threads, synchronization, thread safety
- **Advanced Topics**: Generics, lambdas, annotations, reflection

### **Difficulty Levels:**
- **🟢 Easy**: Fundamental concepts, basic syntax rules
- **🟡 Medium**: Practical application, intermediate topics
- **🔴 Hard**: Advanced concepts, complex problem-solving

---

## 📊 **Player Progression**

### **XP System:**
- **Level 1 → 2**: 100 XP (achievable with 1 perfect quest)
- **Level 2 → 3**: 250 XP total (2-3 good quests)
- **Level 3 → 4**: 450 XP total (continued learning)
- **Exponential Growth**: Higher levels require more dedication

### **Learning Optimization:**
- **Weighted Selection**: Less-practiced questions appear more often
- **Category Balance**: Questions from all Java topics
- **Spaced Repetition**: Smart algorithm for optimal retention

---

## 🏆 **Success Metrics**

After playing FlashQuest, you should notice:
- **🧠 Better Java Knowledge**: Reinforced through active recall
- **📈 Measurable Progress**: XP and level system tracks improvement  
- **🎯 Focused Learning**: Targeted practice on weak areas
- **💪 Confident Coding**: Solid foundation in Java fundamentals

---

## 🚀 **Ready to Launch!**

**Your complete educational RPG awaits!**

```bash
# Let's start your adventure:
mvn javafx:run
```

**Welcome to FlashQuest - Where Learning Becomes an Adventure!** ⚔️📚✨

---

## 🆘 **Need Help?**

If you encounter any issues:
1. Check that Java 17+ is installed: `java --version`
2. Verify Maven is available: `mvn --version`  
3. Ensure you're in the project directory
4. Try the alternative launch methods above
5. Check the console output for specific error messages

**Happy Learning, Hero!** 🎮🎓