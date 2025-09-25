package com.flashquest.service;

import com.flashquest.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service layer for FlashQuest game operations.
 * Coordinates between UI, data persistence, and game logic.
 */
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    
    private static GameService instance;
    
    private final DataService dataService;
    private Player currentPlayer;
    private List<Flashcard> allFlashcards;
    private List<Quest> savedQuests;
    private Quest activeQuest;
    private boolean gameInitialized;

    private GameService() {
        this.dataService = new DataService();
        this.allFlashcards = new ArrayList<>();
        this.savedQuests = new ArrayList<>();
        this.gameInitialized = false;
    }

    /**
     * Gets the singleton instance of GameService.
     */
    public static synchronized GameService getInstance() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }

    /**
     * Initializes the game on first startup.
     * Loads existing data or creates defaults.
     */
    public void initializeGame() {
        if (gameInitialized) return;
        
        try {
            logger.info("Initializing FlashQuest game...");
            
            // Load existing data
            loadGameData();
            
            // If no player exists, we'll need first-time setup
            if (currentPlayer == null) {
                logger.info("No existing player found - first-time setup required");
            } else {
                logger.info("Loaded existing player: {} (Level {}, {} XP)", 
                    currentPlayer.getName(), currentPlayer.getCurrentLevel(), currentPlayer.getTotalXp());
            }
            
            // Ensure we have default flashcards
            if (allFlashcards.isEmpty()) {
                logger.info("No flashcards found - loading defaults");
                loadDefaultFlashcards();
            }
            
            gameInitialized = true;
            logger.info("Game initialization complete");
            
        } catch (Exception e) {
            logger.error("Failed to initialize game", e);
            throw new RuntimeException("Game initialization failed", e);
        }
    }

    /**
     * Creates a new player and initializes their profile.
     */
    public void createNewPlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        
        logger.info("Creating new player: {}", playerName);
        
        currentPlayer = new Player(playerName.trim());
        
        // Ensure default flashcards are loaded
        if (allFlashcards.isEmpty()) {
            loadDefaultFlashcards();
        }
        
        saveGameData();
        
        logger.info("New player created successfully: {}", currentPlayer);
    }

    /**
     * Loads existing game data from storage.
     */
    private void loadGameData() {
        try {
            // Load player
            if (dataService.playerExists()) {
                currentPlayer = dataService.loadPlayer();
            }
            
            // Load flashcards
            allFlashcards = dataService.loadFlashcards();
            
            // Load quests
            savedQuests = dataService.loadQuests();
            
        } catch (Exception e) {
            logger.error("Error loading game data", e);
            // Continue with empty data rather than crash
            currentPlayer = null;
            allFlashcards = new ArrayList<>();
            savedQuests = new ArrayList<>();
        }
    }

    /**
     * Saves all current game data.
     */
    public void saveGameData() {
        try {
            if (currentPlayer != null) {
                dataService.savePlayer(currentPlayer);
            }
            dataService.saveFlashcards(allFlashcards);
            dataService.saveQuests(savedQuests);
            
        } catch (Exception e) {
            logger.error("Failed to save game data", e);
            throw new RuntimeException("Failed to save game data", e);
        }
    }

    /**
     * Loads default Java flashcards.
     */
    private void loadDefaultFlashcards() {
        List<Flashcard> defaultCards = DefaultFlashcardService.createDefaultFlashcards();
        allFlashcards.addAll(defaultCards);
        logger.info("Loaded {} default flashcards", defaultCards.size());
    }

    /**
     * Starts a new quick quest with default parameters.
     */
    public Quest startQuickQuest() {
        return startQuest("Quick Quest", 10, new ArrayList<>(), DifficultyDistribution.balanced());
    }

    /**
     * Starts a quest with specified parameters.
     */
    public Quest startQuest(String name, int questionCount, List<String> categoryFilter, DifficultyDistribution difficulty) {
        if (currentPlayer == null) {
            throw new IllegalStateException("No player loaded");
        }
        
        if (allFlashcards.isEmpty()) {
            throw new IllegalStateException("No flashcards available");
        }
        
        // Create quest
        Quest quest = new Quest(name);
        quest.setQuestionCount(questionCount);
        quest.setCategoryFilter(categoryFilter);
        quest.setDifficultyDistribution(difficulty);
        
        // Select flashcards for the quest
        List<Flashcard> selectedCards = selectQuestFlashcards(quest);
        
        if (selectedCards.size() < questionCount) {
            throw new IllegalStateException("Not enough flashcards available for quest. Need " + 
                questionCount + ", found " + selectedCards.size());
        }
        
        // Start the quest
        quest.startQuest(selectedCards);
        activeQuest = quest;
        
        // Reset player HP for quest
        currentPlayer.restoreFullHp();
        if (quest.getCustomHp() != 3) {
            currentPlayer.setCustomHp(quest.getCustomHp());
        }
        
        logger.info("Started quest: {} with {} questions", name, selectedCards.size());
        return quest;
    }

    /**
     * Selects flashcards for a quest based on quest parameters.
     */
    private List<Flashcard> selectQuestFlashcards(Quest quest) {
        List<Flashcard> availableCards = allFlashcards.stream()
            .filter(card -> quest.getCategoryFilter().isEmpty() || 
                           quest.getCategoryFilter().contains(card.getCategory()))
            .collect(Collectors.toList());
        
        if (availableCards.isEmpty()) {
            availableCards = new ArrayList<>(allFlashcards);
        }
        
        // Apply weighted selection based on usage frequency
        availableCards.sort((a, b) -> Double.compare(b.getSelectionWeight(), a.getSelectionWeight()));
        
        // Select required number of cards (or all available if less)
        int cardsNeeded = Math.min(quest.getQuestionCount(), availableCards.size());
        
        // For now, use simple selection - could be enhanced with smarter algorithms
        List<Flashcard> selected = new ArrayList<>();
        Random random = new Random();
        
        while (selected.size() < cardsNeeded && !availableCards.isEmpty()) {
            // Weighted random selection favoring higher weight cards
            double totalWeight = availableCards.stream().mapToDouble(Flashcard::getSelectionWeight).sum();
            double randomValue = random.nextDouble() * totalWeight;
            
            double currentWeight = 0;
            for (int i = 0; i < availableCards.size(); i++) {
                currentWeight += availableCards.get(i).getSelectionWeight();
                if (randomValue <= currentWeight) {
                    selected.add(availableCards.remove(i));
                    break;
                }
            }
        }
        
        return selected;
    }

    /**
     * Processes an answer during an active quest.
     */
    public Quest.QuestionResult processQuestAnswer(boolean correct) {
        if (activeQuest == null || !activeQuest.isActive()) {
            throw new IllegalStateException("No active quest");
        }
        
        if (currentPlayer == null) {
            throw new IllegalStateException("No player loaded");
        }
        
        Flashcard currentCard = activeQuest.getCurrentFlashcard();
        if (currentCard != null) {
            // Record answer for flashcard statistics
            currentCard.recordAnswer(correct);
            
            // Track Java questions for achievements
            if (isJavaCategory(currentCard.getCategory())) {
                if (correct) {
                    currentPlayer.recordJavaQuestionCorrect();
                }
            }
            
            // Apply HP loss for incorrect answers
            if (!correct) {
                boolean died = currentPlayer.takeDamage(1);
                if (died) {
                    // Quest failed due to HP loss
                    activeQuest = null;
                    saveGameData();
                    return new Quest.QuestionResult(0, false, true);
                }
            }
        }
        
        // Process answer in quest
        Quest.QuestionResult result = activeQuest.processAnswer(correct);
        
        if (result.isQuestComplete()) {
            completeQuest();
        }
        
        return result;
    }

    /**
     * Completes the active quest and awards XP.
     */
    private void completeQuest() {
        if (activeQuest == null || currentPlayer == null) {
            return;
        }
        
        // Award XP to player
        int questXp = activeQuest.getTotalXpEarned();
        boolean leveledUp = currentPlayer.addXp(questXp);
        
        // Record quest completion for achievements
        boolean isPerfect = activeQuest.isPerfectQuest();
        currentPlayer.completeQuest(isPerfect);
        
        // Check for Survivor achievement (complete with exactly 1 HP)
        if (currentPlayer.getCurrentHp() == 1 && 
            !currentPlayer.getUnlockedTitles().contains("Survivor")) {
            currentPlayer.getUnlockedTitles().add("Survivor");
        }
        
        logger.info("Quest completed: {} XP awarded, leveled up: {}", questXp, leveledUp);
        
        activeQuest = null;
        saveGameData();
    }

    /**
     * Adds a new flashcard to the collection.
     */
    public void addFlashcard(String question, String answer, String category, DifficultyLevel difficulty) {
        Flashcard card = new Flashcard(question, answer, category, difficulty);
        allFlashcards.add(card);
        
        if (currentPlayer != null) {
            currentPlayer.recordFlashcardCreated();
        }
        
        saveGameData();
        logger.info("Added new flashcard: {}", card);
    }

    /**
     * Imports flashcards from a text file.
     * 
     * @param file The text file to import from
     * @return ImportResult containing statistics and details about the import
     */
    public FlashcardImportService.ImportResult importFlashcardsFromFile(java.io.File file) {
        FlashcardImportService importService = new FlashcardImportService();
        
        // Get existing question texts for duplicate detection
        Set<String> existingQuestions = allFlashcards.stream()
            .map(card -> card.getQuestion().toLowerCase().trim())
            .collect(java.util.stream.Collectors.toSet());
        
        // Perform the import
        FlashcardImportService.ImportResult result = importService.importFromFile(file, existingQuestions);
        
        // Add successfully imported cards to the collection
        if (!result.getImportedCards().isEmpty()) {
            allFlashcards.addAll(result.getImportedCards());
            
            // Update player stats if player exists
            if (currentPlayer != null) {
                for (int i = 0; i < result.getSuccessfulImports(); i++) {
                    currentPlayer.recordFlashcardCreated();
                }
            }
            
            saveGameData();
            logger.info("Successfully imported {} flashcards from {}", 
                result.getSuccessfulImports(), file.getName());
        }
        
        return result;
    }

    /**
     * Adds multiple flashcards at once (bulk operation).
     * 
     * @param flashcards List of flashcards to add
     * @return Number of successfully added flashcards
     */
    public int addFlashcards(List<Flashcard> flashcards) {
        if (flashcards == null || flashcards.isEmpty()) {
            return 0;
        }
        
        int addedCount = 0;
        for (Flashcard card : flashcards) {
            // Check for duplicates
            boolean isDuplicate = allFlashcards.stream()
                .anyMatch(existing -> existing.getQuestion().toLowerCase().trim()
                    .equals(card.getQuestion().toLowerCase().trim()));
            
            if (!isDuplicate) {
                allFlashcards.add(card);
                addedCount++;
                
                if (currentPlayer != null) {
                    currentPlayer.recordFlashcardCreated();
                }
            }
        }
        
        if (addedCount > 0) {
            saveGameData();
            logger.info("Added {} flashcards in bulk operation", addedCount);
        }
        
        return addedCount;
    }

    /**
     * Checks if a category is Java-related for achievement tracking.
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

    // Getters
    public Player getCurrentPlayer() { return currentPlayer; }
    public List<Flashcard> getAllFlashcards() { return new ArrayList<>(allFlashcards); }
    public List<Quest> getSavedQuests() { return new ArrayList<>(savedQuests); }
    public Quest getActiveQuest() { return activeQuest; }
    public boolean isGameInitialized() { return gameInitialized; }
    public boolean hasPlayer() { return currentPlayer != null; }

    /**
     * Gets available categories from all flashcards.
     */
    public List<String> getAvailableCategories() {
        return allFlashcards.stream()
            .map(Flashcard::getCategory)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Creates a backup of all game data.
     */
    public void createBackup() {
        dataService.createBackup();
    }

    /**
     * Gets total save data size in bytes.
     */
    public long getSaveDataSize() {
        return dataService.getSaveDataSize();
    }
    
    /**
     * Deletes all save data and resets the game state.
     */
    public void deleteAllData() {
        dataService.deleteAllData();
        
        // Reset game state
        currentPlayer = null;
        allFlashcards.clear();
        savedQuests.clear();
        activeQuest = null;
        gameInitialized = false;
        
        logger.info("All game data deleted and state reset");
    }
}