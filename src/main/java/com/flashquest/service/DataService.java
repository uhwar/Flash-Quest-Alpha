package com.flashquest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flashquest.model.Flashcard;
import com.flashquest.model.Player;
import com.flashquest.model.Quest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles data persistence for FlashQuest using JSON files.
 * Manages player profiles, flashcards, and quests.
 */
public class DataService {
    private static final Logger logger = LoggerFactory.getLogger(DataService.class);
    
    // File paths
    private static final String DATA_DIR = "flashquest-data";
    private static final String PLAYER_FILE = "player.json";
    private static final String FLASHCARDS_FILE = "flashcards.json";
    private static final String QUESTS_FILE = "quests.json";
    private static final String BACKUP_SUFFIX = ".backup";
    
    private final ObjectMapper objectMapper;
    private final Path dataDirectory;
    
    /**
     * Creates a new DataService and initializes data directory.
     */
    public DataService() {
        this.objectMapper = createObjectMapper();
        this.dataDirectory = initializeDataDirectory();
    }
    
    /**
     * Creates and configures the Jackson ObjectMapper for JSON serialization.
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Ignore unknown properties for schema migration
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
    
    /**
     * Initializes the data directory in the user's home directory.
     */
    private Path initializeDataDirectory() {
        String userHome = System.getProperty("user.home");
        Path dataDir = Paths.get(userHome, DATA_DIR);
        
        try {
            Files.createDirectories(dataDir);
            logger.info("Data directory initialized: {}", dataDir.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to create data directory: {}", dataDir.toAbsolutePath(), e);
            throw new RuntimeException("Cannot initialize data directory", e);
        }
        
        return dataDir;
    }
    
    /**
     * Saves player data to JSON file with backup.
     */
    public void savePlayer(Player player) {
        Path playerFile = dataDirectory.resolve(PLAYER_FILE);
        saveToFileWithBackup(player, playerFile, "player data");
    }
    
    /**
     * Loads player data from JSON file.
     */
    public Player loadPlayer() {
        Path playerFile = dataDirectory.resolve(PLAYER_FILE);
        return loadFromFile(playerFile, Player.class, "player data");
    }
    
    /**
     * Checks if a player save file exists.
     */
    public boolean playerExists() {
        Path playerFile = dataDirectory.resolve(PLAYER_FILE);
        return Files.exists(playerFile);
    }
    
    /**
     * Saves flashcard list to JSON file with backup.
     */
    public void saveFlashcards(List<Flashcard> flashcards) {
        Path flashcardsFile = dataDirectory.resolve(FLASHCARDS_FILE);
        FlashcardCollection collection = new FlashcardCollection(flashcards);
        saveToFileWithBackup(collection, flashcardsFile, "flashcards");
    }
    
    /**
     * Loads flashcard list from JSON file.
     */
    public List<Flashcard> loadFlashcards() {
        Path flashcardsFile = dataDirectory.resolve(FLASHCARDS_FILE);
        FlashcardCollection collection = loadFromFile(flashcardsFile, FlashcardCollection.class, "flashcards");
        return collection != null ? collection.getFlashcards() : new ArrayList<>();
    }
    
    /**
     * Saves quest list to JSON file with backup.
     */
    public void saveQuests(List<Quest> quests) {
        Path questsFile = dataDirectory.resolve(QUESTS_FILE);
        QuestCollection collection = new QuestCollection(quests);
        saveToFileWithBackup(collection, questsFile, "quests");
    }
    
    /**
     * Loads quest list from JSON file.
     */
    public List<Quest> loadQuests() {
        Path questsFile = dataDirectory.resolve(QUESTS_FILE);
        QuestCollection collection = loadFromFile(questsFile, QuestCollection.class, "quests");
        return collection != null ? collection.getQuests() : new ArrayList<>();
    }
    
    /**
     * Creates a backup of all data files.
     */
    public void createBackup() {
        try {
            backupFile(PLAYER_FILE);
            backupFile(FLASHCARDS_FILE);
            backupFile(QUESTS_FILE);
            logger.info("Backup created successfully");
        } catch (Exception e) {
            logger.error("Failed to create backup", e);
            throw new RuntimeException("Backup creation failed", e);
        }
    }
    
    /**
     * Restores data from backup files.
     */
    public void restoreFromBackup() {
        try {
            restoreFile(PLAYER_FILE);
            restoreFile(FLASHCARDS_FILE);
            restoreFile(QUESTS_FILE);
            logger.info("Data restored from backup successfully");
        } catch (Exception e) {
            logger.error("Failed to restore from backup", e);
            throw new RuntimeException("Backup restoration failed", e);
        }
    }
    
    /**
     * Deletes all save data (for reset functionality).
     */
    public void deleteAllData() {
        try {
            deleteFileIfExists(PLAYER_FILE);
            deleteFileIfExists(FLASHCARDS_FILE);
            deleteFileIfExists(QUESTS_FILE);
            deleteFileIfExists(PLAYER_FILE + BACKUP_SUFFIX);
            deleteFileIfExists(FLASHCARDS_FILE + BACKUP_SUFFIX);
            deleteFileIfExists(QUESTS_FILE + BACKUP_SUFFIX);
            logger.info("All save data deleted");
        } catch (Exception e) {
            logger.error("Failed to delete save data", e);
            throw new RuntimeException("Data deletion failed", e);
        }
    }
    /**
     * Gets total save data size in bytes.
     */
    public long getSaveDataSize() {
        long totalSize = 0;
        try {
            totalSize += getFileSize(PLAYER_FILE);
            totalSize += getFileSize(FLASHCARDS_FILE);
            totalSize += getFileSize(QUESTS_FILE);
        } catch (Exception e) {
            logger.warn("Error calculating save data size", e);
        }
        return totalSize;
    }
    
    /**
     * Gets a list of all available player save files.
     */
    public List<SaveInfo> getAvailableSaves() {
        List<SaveInfo> saves = new ArrayList<>();
        
        try {
            // Check for default player save
            Path playerFile = dataDirectory.resolve(PLAYER_FILE);
            if (Files.exists(playerFile)) {
                try {
                    Player player = objectMapper.readValue(playerFile.toFile(), Player.class);
                    long lastModified = Files.getLastModifiedTime(playerFile).toMillis();
                    saves.add(new SaveInfo(player.getName(), player.getCurrentLevel(), 
                        player.getTotalXp(), player.getQuestsCompleted(), lastModified, "default"));
                } catch (Exception e) {
                    logger.warn("Failed to read player save: {}", playerFile, e);
                    // Add corrupted save entry
                    long lastModified = Files.getLastModifiedTime(playerFile).toMillis();
                    saves.add(new SaveInfo("Corrupted Save", 0, 0, 0, lastModified, "default"));
                }
            }
            
            // Sort by last modified time (most recent first)
            saves.sort((a, b) -> Long.compare(b.getLastModified(), a.getLastModified()));
            
        } catch (Exception e) {
            logger.error("Error scanning for save files", e);
        }
        
        return saves;
    }
    
    /**
     * Information about a save file.
     */
    public static class SaveInfo {
        private final String playerName;
        private final int level;
        private final int totalXp;
        private final int questsCompleted;
        private final long lastModified;
        private final String saveId;
        
        public SaveInfo(String playerName, int level, int totalXp, int questsCompleted, long lastModified, String saveId) {
            this.playerName = playerName;
            this.level = level;
            this.totalXp = totalXp;
            this.questsCompleted = questsCompleted;
            this.lastModified = lastModified;
            this.saveId = saveId;
        }
        
        public String getPlayerName() { return playerName; }
        public int getLevel() { return level; }
        public int getTotalXp() { return totalXp; }
        public int getQuestsCompleted() { return questsCompleted; }
        public long getLastModified() { return lastModified; }
        public String getSaveId() { return saveId; }
        
        public String getFormattedLastModified() {
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastModified), 
                java.time.ZoneId.systemDefault());
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        }
        
        @Override
        public String toString() {
            return String.format("%s (Level %d, %d XP, %d Quests)", 
                playerName, level, totalXp, questsCompleted);
        }
    }
    // Private helper methods
    
    private <T> void saveToFileWithBackup(T object, Path filePath, String dataType) {
        try {
            // Create backup if file exists
            if (Files.exists(filePath)) {
                Path backupPath = Paths.get(filePath.toString() + BACKUP_SUFFIX);
                Files.copy(filePath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Save new data
            objectMapper.writeValue(filePath.toFile(), object);
            logger.debug("Saved {} to {}", dataType, filePath.getFileName());
            
        } catch (IOException e) {
            logger.error("Failed to save {} to {}", dataType, filePath.getFileName(), e);
            throw new RuntimeException("Failed to save " + dataType, e);
        }
    }
    
    private <T> T loadFromFile(Path filePath, Class<T> clazz, String dataType) {
        if (!Files.exists(filePath)) {
            logger.debug("No {} file found: {}", dataType, filePath.getFileName());
            return null;
        }
        
        try {
            T result = objectMapper.readValue(filePath.toFile(), clazz);
            logger.debug("Loaded {} from {}", dataType, filePath.getFileName());
            return result;
            
        } catch (IOException e) {
            logger.error("Failed to load {} from {}", dataType, filePath.getFileName(), e);
            
            // Try to restore from backup
            try {
                Path backupPath = Paths.get(filePath.toString() + BACKUP_SUFFIX);
                if (Files.exists(backupPath)) {
                    logger.info("Attempting to restore {} from backup", dataType);
                    T result = objectMapper.readValue(backupPath.toFile(), clazz);
                    logger.info("Successfully restored {} from backup", dataType);
                    return result;
                }
            } catch (IOException backupError) {
                logger.error("Failed to restore {} from backup", dataType, backupError);
            }
            
            throw new RuntimeException("Failed to load " + dataType, e);
        }
    }
    
    private void backupFile(String filename) throws IOException {
        Path sourcePath = dataDirectory.resolve(filename);
        Path backupPath = dataDirectory.resolve(filename + BACKUP_SUFFIX);
        
        if (Files.exists(sourcePath)) {
            Files.copy(sourcePath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    private void restoreFile(String filename) throws IOException {
        Path targetPath = dataDirectory.resolve(filename);
        Path backupPath = dataDirectory.resolve(filename + BACKUP_SUFFIX);
        
        if (Files.exists(backupPath)) {
            Files.copy(backupPath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    private void deleteFileIfExists(String filename) throws IOException {
        Path filePath = dataDirectory.resolve(filename);
        Files.deleteIfExists(filePath);
    }
    
    private long getFileSize(String filename) {
        Path filePath = dataDirectory.resolve(filename);
        try {
            return Files.exists(filePath) ? Files.size(filePath) : 0;
        } catch (IOException e) {
            return 0;
        }
    }
    
    // Wrapper classes for JSON serialization
    
    /**
     * Wrapper class for serializing flashcard collections.
     */
    public static class FlashcardCollection {
        private List<Flashcard> flashcards;
        
        public FlashcardCollection() {
            this.flashcards = new ArrayList<>();
        }
        
        public FlashcardCollection(List<Flashcard> flashcards) {
            this.flashcards = flashcards != null ? flashcards : new ArrayList<>();
        }
        
        public List<Flashcard> getFlashcards() {
            return flashcards;
        }
        
        public void setFlashcards(List<Flashcard> flashcards) {
            this.flashcards = flashcards;
        }
    }
    
    /**
     * Wrapper class for serializing quest collections.
     */
    public static class QuestCollection {
        private List<Quest> quests;
        
        public QuestCollection() {
            this.quests = new ArrayList<>();
        }
        
        public QuestCollection(List<Quest> quests) {
            this.quests = quests != null ? quests : new ArrayList<>();
        }
        
        public List<Quest> getQuests() {
            return quests;
        }
        
        public void setQuests(List<Quest> quests) {
            this.quests = quests;
        }
    }
}