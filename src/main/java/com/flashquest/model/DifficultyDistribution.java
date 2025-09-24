package com.flashquest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents difficulty distribution settings for quest generation.
 * Controls the percentage mix of Easy/Medium/Hard questions in a quest.
 */
public class DifficultyDistribution {
    private int easyPercentage;
    private int mediumPercentage;
    private int hardPercentage;

    /**
     * Creates a default balanced distribution (40% Easy, 40% Medium, 20% Hard).
     */
    public DifficultyDistribution() {
        this.easyPercentage = 40;
        this.mediumPercentage = 40;
        this.hardPercentage = 20;
    }

    /**
     * Creates a custom distribution.
     * Percentages will be normalized to sum to 100.
     * Also serves as JSON deserialization constructor.
     */
    @JsonCreator
    public DifficultyDistribution(
            @JsonProperty("easyPercentage") int easyPercentage,
            @JsonProperty("mediumPercentage") int mediumPercentage,
            @JsonProperty("hardPercentage") int hardPercentage) {
        setDistribution(easyPercentage, mediumPercentage, hardPercentage);
    }

    /**
     * Sets the distribution and normalizes percentages to sum to 100.
     */
    public void setDistribution(int easy, int medium, int hard) {
        int total = easy + medium + hard;
        if (total == 0) {
            // Default distribution if all are zero
            this.easyPercentage = 40;
            this.mediumPercentage = 40;
            this.hardPercentage = 20;
        } else {
            // Normalize to 100%
            this.easyPercentage = Math.max(0, (easy * 100) / total);
            this.mediumPercentage = Math.max(0, (medium * 100) / total);
            this.hardPercentage = Math.max(0, 100 - this.easyPercentage - this.mediumPercentage);
        }
    }

    /**
     * Creates an easy-focused distribution (70% Easy, 25% Medium, 5% Hard).
     */
    public static DifficultyDistribution easyFocus() {
        return new DifficultyDistribution(70, 25, 5);
    }

    /**
     * Creates a medium-focused distribution (20% Easy, 60% Medium, 20% Hard).
     */
    public static DifficultyDistribution mediumFocus() {
        return new DifficultyDistribution(20, 60, 20);
    }

    /**
     * Creates a hard-focused distribution (10% Easy, 30% Medium, 60% Hard).
     */
    public static DifficultyDistribution hardFocus() {
        return new DifficultyDistribution(10, 30, 60);
    }

    /**
     * Creates a balanced distribution (33% each, roughly).
     */
    public static DifficultyDistribution balanced() {
        return new DifficultyDistribution(34, 33, 33);
    }

    /**
     * Determines how many questions of each difficulty should be in a quest.
     * @param totalQuestions total number of questions in the quest
     * @return array with [easy count, medium count, hard count]
     */
    public int[] calculateQuestionCounts(int totalQuestions) {
        int easyCount = (easyPercentage * totalQuestions) / 100;
        int mediumCount = (mediumPercentage * totalQuestions) / 100;
        int hardCount = totalQuestions - easyCount - mediumCount; // Remainder goes to hard
        
        return new int[]{easyCount, mediumCount, hardCount};
    }

    /**
     * Gets the difficulty level that should be used for a question at the given index.
     * This provides a deterministic way to distribute difficulties across a quest.
     */
    public DifficultyLevel getDifficultyForQuestionIndex(int questionIndex, int totalQuestions) {
        int[] counts = calculateQuestionCounts(totalQuestions);
        
        if (questionIndex < counts[0]) {
            return DifficultyLevel.EASY;
        } else if (questionIndex < counts[0] + counts[1]) {
            return DifficultyLevel.MEDIUM;
        } else {
            return DifficultyLevel.HARD;
        }
    }

    public int getEasyPercentage() { return easyPercentage; }
    public void setEasyPercentage(int easyPercentage) { 
        setDistribution(easyPercentage, mediumPercentage, hardPercentage); 
    }
    
    public int getMediumPercentage() { return mediumPercentage; }
    public void setMediumPercentage(int mediumPercentage) { 
        setDistribution(easyPercentage, mediumPercentage, hardPercentage); 
    }
    
    public int getHardPercentage() { return hardPercentage; }
    public void setHardPercentage(int hardPercentage) { 
        setDistribution(easyPercentage, mediumPercentage, hardPercentage); 
    }

    @Override
    public String toString() {
        return String.format("DifficultyDistribution{Easy: %d%%, Medium: %d%%, Hard: %d%%}", 
            easyPercentage, mediumPercentage, hardPercentage);
    }
}