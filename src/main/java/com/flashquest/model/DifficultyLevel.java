package com.flashquest.model;

/**
 * Represents the difficulty levels for flashcards in FlashQuest.
 * Each level provides different XP bonuses when answered correctly.
 */
public enum DifficultyLevel {
    EASY("Easy", 0, "🟢"),
    MEDIUM("Medium", 5, "🟡"), 
    HARD("Hard", 10, "🔴");

    private final String displayName;
    private final int xpBonus;
    private final String emoji;

    DifficultyLevel(String displayName, int xpBonus, String emoji) {
        this.displayName = displayName;
        this.xpBonus = xpBonus;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getXpBonus() {
        return xpBonus;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayText() {
        return emoji + " " + displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}