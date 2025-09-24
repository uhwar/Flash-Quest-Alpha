package com.flashquest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the player with RPG progression mechanics.
 * Follows the FlashQuest Agent Rule Set XP calculation formula.
 */
public class Player {
    private String id; // Database ID for save system
    private String name;
    private int currentLevel;
    private int totalXp;
    private int currentHp;
    private int maxHp;
    private int customHp; // For Quest Designer feature
    private int questsCompleted;
    private int perfectQuests;
    private int flashcardsCreated;
    private int javaQuestionsCorrect;
    private LocalDateTime dateCreated;
    private String activeTitle;
    private List<String> unlockedTitles;

    /**
     * Creates a new player with starting stats.
     */
    public Player(String name) {
        this.id = java.util.UUID.randomUUID().toString(); // Generate unique ID
        this.name = name;
        this.currentLevel = 1;
        this.totalXp = 0;
        this.currentHp = 3; // Starting HP as per rules
        this.maxHp = 3;
        this.customHp = 3;
        this.questsCompleted = 0;
        this.perfectQuests = 0;
        this.flashcardsCreated = 0;
        this.javaQuestionsCorrect = 0;
        this.dateCreated = LocalDateTime.now();
        this.activeTitle = null;
        this.unlockedTitles = new ArrayList<>();
    }

    /**
     * Constructor for JSON deserialization.
     */
    @JsonCreator
    public Player(
            @JsonProperty("name") String name,
            @JsonProperty("currentLevel") int currentLevel,
            @JsonProperty("totalXp") int totalXp,
            @JsonProperty("currentHp") int currentHp,
            @JsonProperty("maxHp") int maxHp,
            @JsonProperty("questsCompleted") int questsCompleted,
            @JsonProperty("perfectQuests") int perfectQuests,
            @JsonProperty("flashcardsCreated") int flashcardsCreated,
            @JsonProperty("javaQuestionsCorrect") int javaQuestionsCorrect,
            @JsonProperty("dateCreated") LocalDateTime dateCreated,
            @JsonProperty("activeTitle") String activeTitle,
            @JsonProperty("unlockedTitles") List<String> unlockedTitles) {
        this.name = name;
        this.currentLevel = Math.max(1, currentLevel);
        this.totalXp = Math.max(0, totalXp);
        this.currentHp = Math.max(0, currentHp);
        this.maxHp = Math.max(3, maxHp);
        this.questsCompleted = Math.max(0, questsCompleted);
        this.perfectQuests = Math.max(0, perfectQuests);
        this.flashcardsCreated = Math.max(0, flashcardsCreated);
        this.javaQuestionsCorrect = Math.max(0, javaQuestionsCorrect);
        this.dateCreated = dateCreated != null ? dateCreated : LocalDateTime.now();
        this.activeTitle = activeTitle;
        this.unlockedTitles = unlockedTitles != null ? unlockedTitles : new ArrayList<>();
    }

    /**
     * Calculates XP required for a specific level.
     * Based on the progression curve: 100, 250, 450, 700, 1000, 1350, 1750...
     * Each level up costs: 100 + (level-2) * 50, summed from level 2 to target level.
     */
    public static int getXpRequiredForLevel(int level) {
        if (level <= 1) return 0;
        
        // Use the closed-form formula: 75*n + 25*n^2 where n = level - 1
        int n = level - 1;
        return 75 * n + 25 * n * n;
    }

    /**
     * Gets XP required for next level.
     */
    public int getXpRequiredForNextLevel() {
        return getXpRequiredForLevel(currentLevel + 1);
    }

    /**
     * Gets XP required for current level.
     */
    public int getXpRequiredForCurrentLevel() {
        return getXpRequiredForLevel(currentLevel);
    }

    /**
     * Gets progress towards next level as percentage (0-100).
     */
    public double getLevelProgress() {
        int currentLevelXp = getXpRequiredForCurrentLevel();
        int nextLevelXp = getXpRequiredForNextLevel();
        int progressXp = totalXp - currentLevelXp;
        int levelXpRange = nextLevelXp - currentLevelXp;
        
        return levelXpRange > 0 ? (double) progressXp / levelXpRange * 100.0 : 0.0;
    }

    /**
     * Adds XP and handles level progression.
     * @param xp amount of XP to add
     * @return true if level increased
     */
    public boolean addXp(int xp) {
        if (xp <= 0) return false;
        
        int oldLevel = currentLevel;
        totalXp += xp;
        
        // Check for level ups
        while (totalXp >= getXpRequiredForNextLevel()) {
            currentLevel++;
        }
        
        return currentLevel > oldLevel;
    }

    /**
     * Takes damage, reducing HP.
     * @param damage amount of HP to lose
     * @return true if player died (HP <= 0)
     */
    public boolean takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - damage);
        return currentHp <= 0;
    }

    /**
     * Restores HP to maximum (between quests).
     */
    public void restoreFullHp() {
        currentHp = maxHp;
    }

    /**
     * Sets custom HP for quest (Quest Designer feature).
     */
    public void setCustomHp(int hp) {
        this.maxHp = Math.max(1, hp);
        this.currentHp = this.maxHp;
    }

    /**
     * Records quest completion and checks for achievements.
     */
    public void completeQuest(boolean isPerfect) {
        questsCompleted++;
        if (isPerfect) {
            perfectQuests++;
        }
        checkAndUnlockAchievements();
    }

    /**
     * Records flashcard creation for achievements.
     */
    public void recordFlashcardCreated() {
        flashcardsCreated++;
        checkAndUnlockAchievements();
    }

    /**
     * Records correct Java question for achievements.
     */
    public void recordJavaQuestionCorrect() {
        javaQuestionsCorrect++;
        checkAndUnlockAchievements();
    }

    /**
     * Checks and unlocks achievements based on current progress.
     */
    private void checkAndUnlockAchievements() {
        // Scholar: Complete 10 quests
        if (questsCompleted >= 10 && !unlockedTitles.contains("Scholar")) {
            unlockedTitles.add("Scholar");
        }
        
        // Perfectionist: Complete 5 perfect quests
        if (perfectQuests >= 5 && !unlockedTitles.contains("Perfectionist")) {
            unlockedTitles.add("Perfectionist");
        }
        
        // Knowledge Seeker: Create 50 custom flashcards
        if (flashcardsCreated >= 50 && !unlockedTitles.contains("Knowledge Seeker")) {
            unlockedTitles.add("Knowledge Seeker");
        }
        
        // Java Master: Answer 100 Java questions correctly
        if (javaQuestionsCorrect >= 100 && !unlockedTitles.contains("Java Master")) {
            unlockedTitles.add("Java Master");
        }
    }

    /**
     * Gets display text for active title with emoji.
     */
    public String getActiveTitleDisplay() {
        if (activeTitle == null) return null;
        return switch (activeTitle) {
            case "Scholar" -> "📚 Scholar";
            case "Perfectionist" -> "💎 Perfectionist";
            case "Survivor" -> "🛡️ Survivor";
            case "Knowledge Seeker" -> "🔍 Knowledge Seeker";
            case "Java Master" -> "☕ Java Master";
            default -> activeTitle;
        };
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getCurrentLevel() { return currentLevel; }
    
    public int getTotalXp() { return totalXp; }
    
    public int getCurrentHp() { return currentHp; }
    
    public int getMaxHp() { return maxHp; }
    
    public int getCustomHp() { return customHp; }
    
    public int getQuestsCompleted() { return questsCompleted; }
    
    public int getPerfectQuests() { return perfectQuests; }
    
    public int getFlashcardsCreated() { return flashcardsCreated; }
    
    public int getJavaQuestionsCorrect() { return javaQuestionsCorrect; }
    
    public LocalDateTime getDateCreated() { return dateCreated; }
    
    public String getActiveTitle() { return activeTitle; }
    public void setActiveTitle(String activeTitle) { this.activeTitle = activeTitle; }
    
    public List<String> getUnlockedTitles() { return new ArrayList<>(unlockedTitles); }

    @Override
    public String toString() {
        return String.format("Player{name='%s', level=%d, xp=%d, hp=%d/%d, titles=%s}",
            name, currentLevel, totalXp, currentHp, maxHp, unlockedTitles.size());
    }
}