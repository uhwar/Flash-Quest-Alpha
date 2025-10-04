package com.flashquest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a quest (learning session) in FlashQuest.
 * Contains quest parameters, flashcards, and progress tracking.
 */
public class Quest {
    private final String id;
    private String name;
    private String description;
    private int questionCount;
    private int customHp;
    private List<String> categoryFilter;
    private DifficultyDistribution difficultyDistribution;
    private LocalDateTime dateCreated;
    
    // Runtime quest state
    private List<Flashcard> questFlashcards;
    private int currentQuestionIndex;
    private int correctAnswers;
    private int totalXpEarned;
    private boolean isActive;
    private boolean isCompleted;

    /**
     * Creates a new quest with default settings.
     */
    public Quest(String name) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.description = "";
        this.questionCount = 10; // Default per rules
        this.customHp = 3; // Default starting HP
        this.categoryFilter = new ArrayList<>();
        this.difficultyDistribution = new DifficultyDistribution();
        this.dateCreated = LocalDateTime.now();
        
        // Runtime state
        this.questFlashcards = new ArrayList<>();
        this.currentQuestionIndex = 0;
        this.correctAnswers = 0;
        this.totalXpEarned = 0;
        this.isActive = false;
        this.isCompleted = false;
    }

    /**
     * Constructor for JSON deserialization.
     */
    @JsonCreator
    public Quest(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("questionCount") int questionCount,
            @JsonProperty("customHp") int customHp,
            @JsonProperty("categoryFilter") List<String> categoryFilter,
            @JsonProperty("difficultyDistribution") DifficultyDistribution difficultyDistribution,
            @JsonProperty("dateCreated") LocalDateTime dateCreated) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.name = name;
        this.description = description != null ? description : "";
        this.questionCount = Math.max(5, Math.min(20, questionCount)); // Clamp to valid range
        this.customHp = Math.max(1, customHp);
        this.categoryFilter = categoryFilter != null ? categoryFilter : new ArrayList<>();
        this.difficultyDistribution = difficultyDistribution != null ? difficultyDistribution : new DifficultyDistribution();
        this.dateCreated = dateCreated != null ? dateCreated : LocalDateTime.now();
        
        // Runtime state (not persisted)
        this.questFlashcards = new ArrayList<>();
        this.currentQuestionIndex = 0;
        this.correctAnswers = 0;
        this.totalXpEarned = 0;
        this.isActive = false;
        this.isCompleted = false;
    }

    /**
     * Starts the quest with provided flashcards.
     */
    public void startQuest(List<Flashcard> selectedFlashcards) {
        this.questFlashcards = new ArrayList<>(selectedFlashcards);
        this.currentQuestionIndex = 0;
        this.correctAnswers = 0;
        this.totalXpEarned = 0;
        this.isActive = true;
        this.isCompleted = false;
    }

    /**
     * Processes an answer and advances to next question.
     * @param correct whether the answer was correct
     * @return quest result containing XP earned and next state
     */
    public QuestionResult processAnswer(boolean correct) {
        if (!isActive || currentQuestionIndex >= questFlashcards.size()) {
            return new QuestionResult(0, false, true);
        }

        Flashcard currentFlashcard = getCurrentFlashcard();
        
        // Calculate XP for this question
        int questionXp = 0;
        if (correct) {
            questionXp = 10 + currentFlashcard.getDifficultyXpBonus(); // Base + difficulty bonus
            correctAnswers++;
            
            // Track Java questions for achievements
            if (isJavaCategory(currentFlashcard.getCategory())) {
                // This will be handled by the service layer
            }
        }

        totalXpEarned += questionXp;
        currentQuestionIndex++;

        // Check if quest is complete
        boolean questComplete = currentQuestionIndex >= questFlashcards.size();
        if (questComplete) {
            completeQuest();
        }

        return new QuestionResult(questionXp, questComplete, false);
    }

    /**
     * Completes the quest and calculates final bonuses.
     */
    private void completeQuest() {
        isActive = false;
        isCompleted = true;
        
        // Add base completion XP
        totalXpEarned += 50;
        
        // Add perfect quest bonus if applicable
        if (correctAnswers == questFlashcards.size() && questFlashcards.size() == questionCount) {
            totalXpEarned += 25;
        }
    }
    
    /**
     * Completes the quest with failure (e.g., when HP reaches zero).
     * No completion bonuses are awarded.
     */
    public void completeWithFailure() {
        isActive = false;
        isCompleted = true; // Still considered complete, just failed
        // No additional XP bonuses for failed quests
    }

    /**
     * Gets the current flashcard being asked.
     */
    public Flashcard getCurrentFlashcard() {
        if (currentQuestionIndex < questFlashcards.size()) {
            return questFlashcards.get(currentQuestionIndex);
        }
        return null;
    }

    /**
     * Checks if this is a perfect quest (all answers correct).
     */
    public boolean isPerfectQuest() {
        return isCompleted && correctAnswers == questFlashcards.size() && questFlashcards.size() == questionCount;
    }

    /**
     * Gets progress as percentage (0-100).
     */
    public double getProgress() {
        if (questFlashcards.isEmpty()) return 0.0;
        return (double) currentQuestionIndex / questFlashcards.size() * 100.0;
    }

    /**
     * Checks if category is Java-related for achievement tracking.
     */
    private boolean isJavaCategory(String category) {
        return category != null && (
            category.toLowerCase().contains("java") ||
            category.equals("Java Basics") ||
            category.equals("Object-Oriented Programming") ||
            category.equals("Collections Framework") ||
            category.equals("Exception Handling") ||
            category.equals("Concurrency") ||
            category.equals("Advanced Topics")
        );
    }

    // Getters and Setters
    public String getId() { return id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { 
        this.questionCount = Math.max(5, Math.min(20, questionCount)); 
    }
    
    public int getCustomHp() { return customHp; }
    public void setCustomHp(int customHp) { this.customHp = Math.max(1, customHp); }
    
    public List<String> getCategoryFilter() { return new ArrayList<>(categoryFilter); }
    public void setCategoryFilter(List<String> categoryFilter) { 
        this.categoryFilter = categoryFilter != null ? categoryFilter : new ArrayList<>(); 
    }
    
    public DifficultyDistribution getDifficultyDistribution() { return difficultyDistribution; }
    public void setDifficultyDistribution(DifficultyDistribution difficultyDistribution) { 
        this.difficultyDistribution = difficultyDistribution != null ? difficultyDistribution : new DifficultyDistribution(); 
    }
    
    public LocalDateTime getDateCreated() { return dateCreated; }
    
    public List<Flashcard> getQuestFlashcards() { return new ArrayList<>(questFlashcards); }
    
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    
    public int getCorrectAnswers() { return correctAnswers; }
    
    public int getTotalXpEarned() { return totalXpEarned; }
    
    public boolean isActive() { return isActive; }
    
    public boolean isCompleted() { return isCompleted; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quest quest = (Quest) o;
        return Objects.equals(id, quest.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Quest{id='%s', name='%s', questions=%d, hp=%d, active=%s}", 
            id.substring(0, 8), name, questionCount, customHp, isActive);
    }

    /**
     * Represents the result of answering a question.
     */
    public static class QuestionResult {
        private final int xpEarned;
        private final boolean questComplete;
        private final boolean error;

        public QuestionResult(int xpEarned, boolean questComplete, boolean error) {
            this.xpEarned = xpEarned;
            this.questComplete = questComplete;
            this.error = error;
        }

        public int getXpEarned() { return xpEarned; }
        public boolean isQuestComplete() { return questComplete; }
        public boolean isError() { return error; }
    }
}