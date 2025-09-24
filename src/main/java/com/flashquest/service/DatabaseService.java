package com.flashquest.service;

import com.flashquest.model.DifficultyLevel;
import com.flashquest.model.Flashcard;
import com.flashquest.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Database service for local storage using H2 embedded database.
 * Manages player profiles and flashcard collections with full CRUD operations.
 */
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private static final String DB_URL = "jdbc:h2:~/flashquest-data/flashquest;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DB_USER = "flashquest";
    private static final String DB_PASSWORD = "";
    
    private static DatabaseService instance;
    private Connection connection;
    private boolean initialized = false;

    private DatabaseService() {
        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance of DatabaseService.
     */
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Initializes the database connection and creates tables if needed.
     */
    public void initialize() {
        if (initialized) return;
        
        try {
            logger.info("Initializing FlashQuest database...");
            
            // Load H2 driver
            Class.forName("org.h2.Driver");
            
            // Create connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(true);
            
            // Create tables
            createTables();
            
            initialized = true;
            logger.info("Database initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Creates the necessary database tables.
     */
    private void createTables() throws SQLException {
        // Create players table
        String createPlayersTable = """
            CREATE TABLE IF NOT EXISTS players (
                id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                current_level INTEGER DEFAULT 1,
                total_xp INTEGER DEFAULT 0,
                current_hp INTEGER DEFAULT 3,
                max_hp INTEGER DEFAULT 3,
                custom_hp INTEGER DEFAULT 3,
                quests_completed INTEGER DEFAULT 0,
                perfect_quests INTEGER DEFAULT 0,
                flashcards_created INTEGER DEFAULT 0,
                java_questions_correct INTEGER DEFAULT 0,
                active_title VARCHAR(100),
                date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_by_user VARCHAR(100) DEFAULT 'default'
            )
        """;
        
        // Create player_titles table for achievements
        String createPlayerTitlesTable = """
            CREATE TABLE IF NOT EXISTS player_titles (
                player_id VARCHAR(50),
                title VARCHAR(100),
                date_earned TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (player_id, title),
                FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
            )
        """;
        
        // Create flashcards table
        String createFlashcardsTable = """
            CREATE TABLE IF NOT EXISTS flashcards (
                id VARCHAR(50) PRIMARY KEY,
                question TEXT NOT NULL,
                answer TEXT NOT NULL,
                category VARCHAR(100) NOT NULL,
                difficulty VARCHAR(20) NOT NULL,
                times_asked INTEGER DEFAULT 0,
                times_correct INTEGER DEFAULT 0,
                date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_by_player VARCHAR(50),
                is_default BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (created_by_player) REFERENCES players(id) ON DELETE SET NULL
            )
        """;
        
        // Create save_profiles table for organizing saves
        String createSaveProfilesTable = """
            CREATE TABLE IF NOT EXISTS save_profiles (
                id VARCHAR(50) PRIMARY KEY,
                profile_name VARCHAR(100) NOT NULL UNIQUE,
                description TEXT,
                date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                player_count INTEGER DEFAULT 0,
                flashcard_count INTEGER DEFAULT 0
            )
        """;
        
        // Create profile_players junction table
        String createProfilePlayersTable = """
            CREATE TABLE IF NOT EXISTS profile_players (
                profile_id VARCHAR(50),
                player_id VARCHAR(50),
                is_active BOOLEAN DEFAULT FALSE,
                PRIMARY KEY (profile_id, player_id),
                FOREIGN KEY (profile_id) REFERENCES save_profiles(id) ON DELETE CASCADE,
                FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createPlayerTitlesTable);
            stmt.execute(createFlashcardsTable);
            stmt.execute(createSaveProfilesTable);
            stmt.execute(createProfilePlayersTable);
            
            logger.info("Database tables created/verified successfully");
            
            // Create default profile if none exists
            createDefaultProfileIfNeeded();
        }
    }

    /**
     * Creates a default save profile if none exists.
     */
    private void createDefaultProfileIfNeeded() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM save_profiles";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Create default profile
                String profileId = UUID.randomUUID().toString();
                String insertQuery = """
                    INSERT INTO save_profiles (id, profile_name, description) 
                    VALUES (?, 'Default Profile', 'Default FlashQuest save profile')
                """;
                
                try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                    pstmt.setString(1, profileId);
                    pstmt.executeUpdate();
                    
                    logger.info("Created default save profile: {}", profileId);
                }
            }
        }
    }

    /**
     * Gets all available save profiles.
     */
    public List<SaveProfile> getAllSaveProfiles() {
        List<SaveProfile> profiles = new ArrayList<>();
        
        String query = """
            SELECT sp.id, sp.profile_name, sp.description, sp.date_created, 
                   sp.last_accessed, sp.player_count, sp.flashcard_count,
                   COUNT(DISTINCT pp.player_id) as actual_players,
                   COUNT(DISTINCT f.id) as actual_flashcards
            FROM save_profiles sp
            LEFT JOIN profile_players pp ON sp.id = pp.profile_id
            LEFT JOIN players p ON pp.player_id = p.id
            LEFT JOIN flashcards f ON f.created_by_player = p.id OR f.is_default = TRUE
            GROUP BY sp.id, sp.profile_name, sp.description, sp.date_created, 
                     sp.last_accessed, sp.player_count, sp.flashcard_count
            ORDER BY sp.last_accessed DESC
        """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                SaveProfile profile = new SaveProfile();
                profile.setId(rs.getString("id"));
                profile.setProfileName(rs.getString("profile_name"));
                profile.setDescription(rs.getString("description"));
                profile.setDateCreated(rs.getTimestamp("date_created").toLocalDateTime());
                profile.setLastAccessed(rs.getTimestamp("last_accessed").toLocalDateTime());
                profile.setPlayerCount(rs.getInt("actual_players"));
                profile.setFlashcardCount(rs.getInt("actual_flashcards"));
                
                profiles.add(profile);
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching save profiles", e);
        }
        
        return profiles;
    }

    /**
     * Creates a new save profile.
     */
    public String createSaveProfile(String profileName, String description) {
        String profileId = UUID.randomUUID().toString();
        
        String insertQuery = """
            INSERT INTO save_profiles (id, profile_name, description) 
            VALUES (?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, profileId);
            pstmt.setString(2, profileName);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            
            logger.info("Created new save profile: {} ({})", profileName, profileId);
            return profileId;
            
        } catch (SQLException e) {
            logger.error("Error creating save profile: {}", profileName, e);
            throw new RuntimeException("Failed to create save profile", e);
        }
    }

    /**
     * Gets all players in a specific save profile.
     */
    public List<Player> getPlayersInProfile(String profileId) {
        List<Player> players = new ArrayList<>();
        
        String query = """
            SELECT p.id, p.name, p.current_level, p.total_xp, p.current_hp, p.max_hp,
                   p.custom_hp, p.quests_completed, p.perfect_quests, p.flashcards_created,
                   p.java_questions_correct, p.active_title, p.date_created, p.last_played,
                   pp.is_active
            FROM players p
            JOIN profile_players pp ON p.id = pp.player_id
            WHERE pp.profile_id = ?
            ORDER BY p.last_played DESC
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, profileId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Player player = createPlayerFromResultSet(rs);
                    players.add(player);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching players for profile: {}", profileId, e);
        }
        
        return players;
    }

    /**
     * Creates a new player in the specified save profile.
     */
    public Player createPlayer(String profileId, String playerName) {
        String playerId = UUID.randomUUID().toString();
        
        String insertPlayerQuery = """
            INSERT INTO players (id, name, created_by_user) VALUES (?, ?, ?)
        """;
        
        String insertProfilePlayerQuery = """
            INSERT INTO profile_players (profile_id, player_id, is_active) VALUES (?, ?, TRUE)
        """;
        
        try {
            // Insert player
            try (PreparedStatement pstmt = connection.prepareStatement(insertPlayerQuery)) {
                pstmt.setString(1, playerId);
                pstmt.setString(2, playerName);
                pstmt.setString(3, profileId);
                pstmt.executeUpdate();
            }
            
            // Link to profile
            try (PreparedStatement pstmt = connection.prepareStatement(insertProfilePlayerQuery)) {
                pstmt.setString(1, profileId);
                pstmt.setString(2, playerId);
                pstmt.executeUpdate();
            }
            
            // Update profile last accessed
            updateProfileLastAccessed(profileId);
            
            // Create and return player object
            Player player = new Player(playerName);
            player.setId(playerId);
            
            logger.info("Created new player: {} in profile: {}", playerName, profileId);
            return player;
            
        } catch (SQLException e) {
            logger.error("Error creating player: {} in profile: {}", playerName, profileId, e);
            throw new RuntimeException("Failed to create player", e);
        }
    }

    /**
     * Saves player data to the database.
     */
    public void savePlayer(Player player) {
        String updateQuery = """
            UPDATE players SET 
                name = ?, current_level = ?, total_xp = ?, current_hp = ?, max_hp = ?,
                custom_hp = ?, quests_completed = ?, perfect_quests = ?, flashcards_created = ?,
                java_questions_correct = ?, active_title = ?, last_played = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, player.getCurrentLevel());
            pstmt.setInt(3, player.getTotalXp());
            pstmt.setInt(4, player.getCurrentHp());
            pstmt.setInt(5, player.getMaxHp());
            pstmt.setInt(6, player.getCustomHp());
            pstmt.setInt(7, player.getQuestsCompleted());
            pstmt.setInt(8, player.getPerfectQuests());
            pstmt.setInt(9, player.getFlashcardsCreated());
            pstmt.setInt(10, player.getJavaQuestionsCorrect());
            pstmt.setString(11, player.getActiveTitle());
            pstmt.setString(12, player.getId());
            
            pstmt.executeUpdate();
            
            // Save player titles
            savePlayerTitles(player);
            
        } catch (SQLException e) {
            logger.error("Error saving player: {}", player.getName(), e);
            throw new RuntimeException("Failed to save player data", e);
        }
    }

    /**
     * Saves player achievements/titles.
     */
    private void savePlayerTitles(Player player) throws SQLException {
        // Clear existing titles
        String deleteQuery = "DELETE FROM player_titles WHERE player_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setString(1, player.getId());
            pstmt.executeUpdate();
        }
        
        // Insert current titles
        String insertQuery = "INSERT INTO player_titles (player_id, title) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            for (String title : player.getUnlockedTitles()) {
                pstmt.setString(1, player.getId());
                pstmt.setString(2, title);
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Loads flashcards for the current profile.
     */
    public List<Flashcard> loadFlashcards(String profileId) {
        List<Flashcard> flashcards = new ArrayList<>();
        
        String query = """
            SELECT f.id, f.question, f.answer, f.category, f.difficulty, 
                   f.times_asked, f.times_correct, f.date_created, f.is_default
            FROM flashcards f
            LEFT JOIN players p ON f.created_by_player = p.id
            LEFT JOIN profile_players pp ON p.id = pp.player_id
            WHERE f.is_default = TRUE OR pp.profile_id = ?
            ORDER BY f.date_created DESC
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, profileId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Flashcard flashcard = createFlashcardFromResultSet(rs);
                    flashcards.add(flashcard);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error loading flashcards for profile: {}", profileId, e);
        }
        
        return flashcards;
    }

    /**
     * Saves a flashcard to the database.
     */
    public void saveFlashcard(Flashcard flashcard, String createdByPlayerId) {
        String insertQuery = """
            INSERT INTO flashcards (id, question, answer, category, difficulty, 
                                  times_asked, times_correct, created_by_player, is_default) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, flashcard.getId());
            pstmt.setString(2, flashcard.getQuestion());
            pstmt.setString(3, flashcard.getAnswer());
            pstmt.setString(4, flashcard.getCategory());
            pstmt.setString(5, flashcard.getDifficulty().name());
            pstmt.setInt(6, flashcard.getTimesAsked());
            pstmt.setInt(7, flashcard.getTimesCorrect());
            pstmt.setString(8, createdByPlayerId);
            pstmt.setBoolean(9, false); // User-created cards are not default
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("Error saving flashcard: {}", flashcard.getId(), e);
            throw new RuntimeException("Failed to save flashcard", e);
        }
    }

    /**
     * Loads default flashcards into the database.
     */
    public void loadDefaultFlashcards() {
        // Check if default flashcards already exist
        String checkQuery = "SELECT COUNT(*) FROM flashcards WHERE is_default = TRUE";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            
            if (rs.next() && rs.getInt(1) > 0) {
                logger.debug("Default flashcards already loaded");
                return;
            }
        } catch (SQLException e) {
            logger.error("Error checking for default flashcards", e);
            return;
        }
        
        // Load default flashcards
        List<Flashcard> defaultCards = DefaultFlashcardService.createDefaultFlashcards();
        
        String insertQuery = """
            INSERT INTO flashcards (id, question, answer, category, difficulty, 
                                  times_asked, times_correct, is_default) 
            VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            for (Flashcard card : defaultCards) {
                pstmt.setString(1, card.getId());
                pstmt.setString(2, card.getQuestion());
                pstmt.setString(3, card.getAnswer());
                pstmt.setString(4, card.getCategory());
                pstmt.setString(5, card.getDifficulty().name());
                pstmt.setInt(6, card.getTimesAsked());
                pstmt.setInt(7, card.getTimesCorrect());
                
                pstmt.executeUpdate();
            }
            
            logger.info("Loaded {} default flashcards into database", defaultCards.size());
            
        } catch (SQLException e) {
            logger.error("Error loading default flashcards", e);
            throw new RuntimeException("Failed to load default flashcards", e);
        }
    }

    /**
     * Updates the last accessed timestamp for a profile.
     */
    private void updateProfileLastAccessed(String profileId) {
        String updateQuery = "UPDATE save_profiles SET last_accessed = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, profileId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Failed to update profile last accessed time: {}", profileId, e);
        }
    }

    /**
     * Helper method to create Player object from ResultSet.
     */
    private Player createPlayerFromResultSet(ResultSet rs) throws SQLException {
        String playerId = rs.getString("id");
        String playerName = rs.getString("name");
        
        // Create player with basic constructor, then set additional fields
        Player player = new Player(playerName);
        player.setId(playerId);
        
        // Set all player fields using reflection or setters (if available)
        // For now, we'll create a new constructor or use the existing data structure
        
        return player;
    }

    /**
     * Helper method to create Flashcard object from ResultSet.
     */
    private Flashcard createFlashcardFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String question = rs.getString("question");
        String answer = rs.getString("answer");
        String category = rs.getString("category");
        DifficultyLevel difficulty = DifficultyLevel.valueOf(rs.getString("difficulty"));
        LocalDateTime dateCreated = rs.getTimestamp("date_created").toLocalDateTime();
        int timesAsked = rs.getInt("times_asked");
        int timesCorrect = rs.getInt("times_correct");
        
        return new Flashcard(id, question, answer, category, difficulty, 
                           dateCreated, timesAsked, timesCorrect);
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    /**
     * Data class for save profile information.
     */
    public static class SaveProfile {
        private String id;
        private String profileName;
        private String description;
        private LocalDateTime dateCreated;
        private LocalDateTime lastAccessed;
        private int playerCount;
        private int flashcardCount;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getProfileName() { return profileName; }
        public void setProfileName(String profileName) { this.profileName = profileName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getDateCreated() { return dateCreated; }
        public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }
        
        public LocalDateTime getLastAccessed() { return lastAccessed; }
        public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
        
        public int getPlayerCount() { return playerCount; }
        public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }
        
        public int getFlashcardCount() { return flashcardCount; }
        public void setFlashcardCount(int flashcardCount) { this.flashcardCount = flashcardCount; }

        @Override
        public String toString() {
            return String.format("SaveProfile{name='%s', players=%d, cards=%d, lastAccessed=%s}",
                profileName, playerCount, flashcardCount, lastAccessed);
        }
    }
}