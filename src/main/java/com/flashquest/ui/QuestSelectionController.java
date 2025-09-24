package com.flashquest.ui;

import com.flashquest.model.Player;
import com.flashquest.service.GameService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the quest selection screen.
 * Allows players to start quick quests or access quest designer (future feature).
 */
public class QuestSelectionController implements Initializable, ScreenController {
    private static final Logger logger = LoggerFactory.getLogger(QuestSelectionController.class);
    
    @FXML private Button backButton;
    @FXML private Button startQuickQuestButton;
    @FXML private Button questDesignerButton;
    
    @FXML private Label quickQuestQuestionsLabel;
    @FXML private Label quickQuestDifficultyLabel;
    @FXML private Label quickQuestCategoriesLabel;
    
    @FXML private Label playerLevelLabel;
    @FXML private Label playerHpLabel;
    @FXML private Label availableFlashcardsLabel;
    @FXML private Label readyStatusLabel;
    
    private AppController appController;
    private GameService gameService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing QuestSelectionController");
        gameService = GameService.getInstance();
    }

    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
        refreshDisplay();
    }

    @Override
    public void onScreenShown() {
        refreshDisplay();
    }

    /**
     * Refreshes the display with current player and game state.
     */
    private void refreshDisplay() {
        Player player = gameService.getCurrentPlayer();
        if (player == null) {
            logger.warn("No player found when refreshing quest selection");
            return;
        }

        try {
            // Update player status
            playerLevelLabel.setText(String.valueOf(player.getCurrentLevel()));
            playerHpLabel.setText(String.format("❤️ %d/%d", player.getCurrentHp(), player.getMaxHp()));
            
            // Update available flashcards count
            int flashcardCount = gameService.getAllFlashcards().size();
            availableFlashcardsLabel.setText(String.valueOf(flashcardCount));
            
            // Update ready status
            if (flashcardCount == 0) {
                readyStatusLabel.setText("⚠️ No Cards");
                readyStatusLabel.getStyleClass().clear();
                readyStatusLabel.getStyleClass().add("error");
                startQuickQuestButton.setDisable(true);
            } else if (player.getCurrentHp() <= 0) {
                readyStatusLabel.setText("💀 No HP");
                readyStatusLabel.getStyleClass().clear();
                readyStatusLabel.getStyleClass().add("error");
                startQuickQuestButton.setDisable(true);
            } else {
                readyStatusLabel.setText("⚔️ Ready!");
                readyStatusLabel.getStyleClass().clear();
                readyStatusLabel.getStyleClass().add("success");
                startQuickQuestButton.setDisable(false);
            }
            
            // Update quest info (these are static for quick quest)
            quickQuestQuestionsLabel.setText("10");
            quickQuestDifficultyLabel.setText("Balanced Mix");
            
            // Show available categories
            var categories = gameService.getAvailableCategories();
            if (categories.size() <= 3) {
                quickQuestCategoriesLabel.setText(String.join(", ", categories));
            } else {
                quickQuestCategoriesLabel.setText(categories.size() + " Categories");
            }
            
        } catch (Exception e) {
            logger.error("Error refreshing quest selection display", e);
        }
    }

    /**
     * Handles the back button click.
     */
    @FXML
    private void onBack() {
        logger.info("Back button clicked");
        appController.showMainMenu();
    }

    /**
     * Handles the start quick quest button click.
     */
    @FXML
    private void onStartQuickQuest() {
        logger.info("Start Quick Quest button clicked");
        
        try {
            // Validate player state
            Player player = gameService.getCurrentPlayer();
            if (player == null) {
                appController.showErrorDialog("Error", "No player found!");
                return;
            }
            
            if (player.getCurrentHp() <= 0) {
                appController.showErrorDialog("No Health", 
                    "You have no health remaining! Health is restored between quests.");
                return;
            }
            
            if (gameService.getAllFlashcards().isEmpty()) {
                appController.showErrorDialog("No Flashcards", 
                    "You need flashcards to start a quest. Please add some flashcards first.");
                return;
            }
            
            // Start the quest
            gameService.startQuickQuest();
            
            // Navigate to quest screen
            appController.showQuest();
            
        } catch (Exception e) {
            logger.error("Error starting quick quest", e);
            appController.showErrorDialog("Error", "Failed to start quest: " + e.getMessage());
        }
    }

    /**
     * Handles the quest designer button click (future feature).
     */
    @FXML
    private void onQuestDesigner() {
        logger.info("Quest Designer button clicked");
        
        appController.showInfoDialog("Coming Soon! 🛠️", 
            "The Quest Designer will allow you to create custom quests with:\n\n" +
            "• Custom question counts (5-20)\n" +
            "• Specific category filters\n" +
            "• Difficulty distribution control\n" +
            "• Custom HP settings\n\n" +
            "For now, enjoy the Quick Quest experience!");
    }
}