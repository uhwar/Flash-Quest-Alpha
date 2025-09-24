package com.flashquest.ui;

import com.flashquest.model.Player;
import com.flashquest.service.GameService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main menu screen.
 * Displays player statistics and provides navigation to other game screens.
 */
public class MainMenuController implements Initializable, ScreenController {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuController.class);
    
    @FXML private Label gameTitle;
    @FXML private Label playerNameLabel;
    @FXML private Label activeTitleLabel;
    @FXML private Label levelLabel;
    @FXML private Label xpLabel;
    @FXML private ProgressBar xpProgressBar;
    @FXML private Label hpLabel;
    @FXML private Label questsCompletedLabel;
    @FXML private Label perfectQuestsLabel;
    @FXML private Label flashcardsCreatedLabel;
    @FXML private Label achievementsLabel;
    
    @FXML private Button startQuestButton;
    @FXML private Button addFlashcardButton;
    @FXML private Button viewStatsButton;
    @FXML private Button manageFlashcardsButton;
    
    private AppController appController;
    private GameService gameService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing MainMenuController");
        gameService = GameService.getInstance();
    }

    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
        refreshPlayerStats();
    }

    @Override
    public void onScreenShown() {
        refreshPlayerStats();
    }

    /**
     * Refreshes all player statistics displayed on the main menu.
     */
    private void refreshPlayerStats() {
        Player player = gameService.getCurrentPlayer();
        if (player == null) {
            logger.warn("No player found when refreshing main menu stats");
            return;
        }

        try {
            // Update player info
            playerNameLabel.setText(player.getName());
            
            // Update active title
            String activeTitle = player.getActiveTitleDisplay();
            if (activeTitle != null && !activeTitle.isEmpty()) {
                activeTitleLabel.setText(activeTitle);
                activeTitleLabel.setVisible(true);
            } else {
                activeTitleLabel.setVisible(false);
            }

            // Update level and XP
            levelLabel.setText(String.valueOf(player.getCurrentLevel()));
            
            int currentXp = player.getTotalXp();
            int currentLevelXp = player.getXpRequiredForCurrentLevel();
            int nextLevelXp = player.getXpRequiredForNextLevel();
            int progressXp = currentXp - currentLevelXp;
            int levelXpRange = nextLevelXp - currentLevelXp;
            
            xpLabel.setText(String.format("%d / %d XP", progressXp, levelXpRange));
            double xpProgress = levelXpRange > 0 ? (double) progressXp / levelXpRange : 0.0;
            xpProgressBar.setProgress(xpProgress);

            // Update HP
            hpLabel.setText(String.format("❤️ %d/%d", player.getCurrentHp(), player.getMaxHp()));

            // Update quest statistics
            questsCompletedLabel.setText(String.valueOf(player.getQuestsCompleted()));
            perfectQuestsLabel.setText(String.valueOf(player.getPerfectQuests()));
            flashcardsCreatedLabel.setText(String.valueOf(player.getFlashcardsCreated()));

            // Update achievements
            int titleCount = player.getUnlockedTitles().size();
            achievementsLabel.setText(String.format("🏆 %d Title%s", titleCount, titleCount == 1 ? "" : "s"));

            logger.debug("Refreshed player stats for: {} (Level {}, {} XP)", 
                player.getName(), player.getCurrentLevel(), player.getTotalXp());

        } catch (Exception e) {
            logger.error("Error refreshing player stats", e);
        }
    }

    /**
     * Handles the Start Quest button click.
     */
    @FXML
    private void onStartQuest() {
        logger.info("Start Quest button clicked");
        
        try {
            // Check if player has any flashcards available
            if (gameService.getAllFlashcards().isEmpty()) {
                appController.showErrorDialog("No Flashcards", 
                    "You need at least one flashcard to start a quest. Try adding some flashcards first!");
                return;
            }
            
            appController.showQuestSelection();
            
        } catch (Exception e) {
            logger.error("Error starting quest selection", e);
            appController.showErrorDialog("Error", "Failed to start quest: " + e.getMessage());
        }
    }

    /**
     * Handles the Add Flashcard button click.
     */
    @FXML
    private void onAddFlashcard() {
        logger.info("Add Flashcard button clicked");
        
        try {
            // For now, show a simple info dialog
            appController.showInfoDialog("Coming Soon! ✨", 
                "The flashcard creation interface is being developed. " +
                "You can currently use the default Java flashcards to start learning!");
            
        } catch (Exception e) {
            logger.error("Error in add flashcard", e);
            appController.showErrorDialog("Error", "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handles the View Stats button click.
     */
    @FXML
    private void onViewStats() {
        logger.info("View Stats button clicked");
        
        try {
            appController.showPlayerStats();
            
        } catch (Exception e) {
            logger.error("Error showing player stats", e);
            appController.showErrorDialog("Error", "Failed to show stats: " + e.getMessage());
        }
    }

    /**
     * Handles the Manage Flashcards button click.
     */
    @FXML
    private void onManageFlashcards() {
        logger.info("Manage Flashcards button clicked");
        
        try {
            appController.showFlashcardManager();
            
        } catch (Exception e) {
            logger.error("Error showing flashcard manager", e);
            appController.showErrorDialog("Error", "Failed to show flashcard manager: " + e.getMessage());
        }
    }
}
