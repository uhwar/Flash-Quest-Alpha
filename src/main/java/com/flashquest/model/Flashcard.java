package com.flashquest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a flashcard with question, answer, and learning statistics.
 * Follows the FlashQuest Agent Rule Set specifications.
 */
public class Flashcard {
    private final String id;
    private String question;
    private String answer;
    private String category;
    private DifficultyLevel difficulty;
    private LocalDateTime dateCreated;
    private int timesAsked;
    private int timesCorrect;

    /**
     * Creates a new flashcard with generated ID and current timestamp.
     */
    public Flashcard(String question, String answer, String category, DifficultyLevel difficulty) {
        this.id = java.util.UUID.randomUUID().toString();
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.difficulty = difficulty;
        this.dateCreated = LocalDateTime.now();
        this.timesAsked = 0;
        this.timesCorrect = 0;
    }

    /**
     * Constructor for JSON deserialization.
     */
    @JsonCreator
    public Flashcard(
            @JsonProperty("id") String id,
            @JsonProperty("question") String question,
            @JsonProperty("answer") String answer,
            @JsonProperty("category") String category,
            @JsonProperty("difficulty") DifficultyLevel difficulty,
            @JsonProperty("dateCreated") LocalDateTime dateCreated,
            @JsonProperty("timesAsked") int timesAsked,
            @JsonProperty("timesCorrect") int timesCorrect) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.difficulty = difficulty;
        this.dateCreated = dateCreated != null ? dateCreated : LocalDateTime.now();
        this.timesAsked = timesAsked;
        this.timesCorrect = timesCorrect;
    }

    /**
     * Records that this flashcard was asked and whether it was answered correctly.
     * Updates statistics for weighted random selection.
     */
    public void recordAnswer(boolean correct) {
        timesAsked++;
        if (correct) {
            timesCorrect++;
        }
    }

    /**
     * Calculates accuracy rate for this flashcard.
     * @return accuracy as percentage (0.0 to 100.0)
     */
    public double getAccuracyRate() {
        return timesAsked > 0 ? (double) timesCorrect / timesAsked * 100.0 : 0.0;
    }

    /**
     * Calculates weight for random selection algorithm.
     * Questions asked less frequently get higher weight.
     * @return weight value for selection algorithm
     */
    public double getSelectionWeight() {
        // Base weight, higher for less frequently asked questions
        double baseWeight = Math.max(1.0, 10.0 - timesAsked * 0.5);
        
        // Slight bonus for incorrect answers to reinforce learning
        double accuracyModifier = timesAsked > 0 ? (100.0 - getAccuracyRate()) * 0.01 + 1.0 : 1.0;
        
        return baseWeight * accuracyModifier;
    }

    /**
     * Gets XP bonus for answering this flashcard correctly based on difficulty.
     * @return bonus XP points
     */
    public int getDifficultyXpBonus() {
        return switch (difficulty) {
            case EASY -> 0;
            case MEDIUM -> 5;
            case HARD -> 10;
        };
    }

    // Getters and Setters
    public String getId() { return id; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }
    
    public LocalDateTime getDateCreated() { return dateCreated; }
    
    public int getTimesAsked() { return timesAsked; }
    
    public int getTimesCorrect() { return timesCorrect; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flashcard flashcard = (Flashcard) o;
        return Objects.equals(id, flashcard.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Flashcard{id='%s', question='%.50s...', category='%s', difficulty=%s, accuracy=%.1f%%}", 
            id.substring(0, 8), question, category, difficulty, getAccuracyRate());
    }
}