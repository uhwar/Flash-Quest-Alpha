package com.flashquest.ui;

import com.flashquest.model.*;
import com.flashquest.service.GameService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the quest gameplay screen.
 * Handles the core flashcard learning experience with RPG mechanics.
 */
public class QuestController implements Initializable, ScreenController {
    private static final Logger logger = LoggerFactory.getLogger(QuestController.class);
    
    // Header elements
    @FXML private Button abandonQuestButton;
    @FXML private Label questNameLabel;
    @FXML private Label questProgressLabel;
    @FXML private Label playerNameLabel;
    @FXML private Label levelLabel;
    @FXML private Label hpLabel;
    @FXML private ProgressBar questProgressBar;
    @FXML private ProgressBar hpProgressBar;
    @FXML private ProgressBar xpProgressBar;
    @FXML private Label questXpLabel;
    @FXML private Label difficultyLabel;
    
    // Question elements
    @FXML private ScrollPane questionScrollPane;
    @FXML private Label questionLabel;
    @FXML private Label categoryLabel;
    
    // Answer elements
    @FXML private VBox answerSection;
    @FXML private Label answerLabel;
    
    // Action buttons
    @FXML private Button showAnswerButton;
    @FXML private HBox assessmentButtons;
    @FXML private Button correctButton;
    @FXML private Button incorrectButton;
    @FXML private Button nextQuestionButton;
    
    private AppController appController;
    private GameService gameService;
    private Quest activeQuest;
    private Flashcard currentFlashcard;
    private Player player;
    private int questXpEarned;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing QuestController");
        gameService = GameService.getInstance();
        questXpEarned = 0;
    }

    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
        loadQuestData();
    }
    
    @Override
    public void onScreenShown() {
        loadQuestData();
    }
    
    /**
     * Loads and displays the active quest data.
     */
    private void loadQuestData() {
        activeQuest = gameService.getActiveQuest();
        player = gameService.getCurrentPlayer();
        
        if (activeQuest == null || !activeQuest.isActive()) {
            logger.warn("No active quest found when loading quest screen");
            appController.showErrorDialog("Error", "No active quest found!");
            appController.showMainMenu();
            return;
        }
        
        if (player == null) {
            logger.warn("No player found when loading quest screen");
            appController.showErrorDialog("Error", "No player found!");
            appController.showMainMenu();
            return;
        }
        
        updateDisplay();
        loadCurrentQuestion();
    }
    
    /**
     * Updates all display elements with current quest and player state.
     */
    private void updateDisplay() {
        // Update header info
        questNameLabel.setText(activeQuest.getName());
        questProgressLabel.setText(String.format("Question %d of %d", 
            activeQuest.getCurrentQuestionIndex() + 1, activeQuest.getQuestionCount()));
        
        playerNameLabel.setText(player.getName());
        levelLabel.setText("Level " + player.getCurrentLevel());
        hpLabel.setText(String.format("❤️ %d/%d", player.getCurrentHp(), player.getMaxHp()));
        
        // Update progress bars
        double questProgress = (double) activeQuest.getCurrentQuestionIndex() / activeQuest.getQuestionCount();
        questProgressBar.setProgress(questProgress);
        
        double hpProgress = (double) player.getCurrentHp() / player.getMaxHp();
        hpProgressBar.setProgress(hpProgress);
        
        // Update XP info
        questXpEarned = activeQuest.getTotalXpEarned();
        questXpLabel.setText(questXpEarned + " XP");
        
        // Update player XP progress
        updatePlayerXpDisplay();
    }
    
    /**
     * Updates the player XP progress bar.
     */
    private void updatePlayerXpDisplay() {
        int currentXp = player.getTotalXp();
        int currentLevelXp = player.getXpRequiredForCurrentLevel();
        int nextLevelXp = player.getXpRequiredForNextLevel();
        int progressXp = currentXp - currentLevelXp;
        int levelXpRange = nextLevelXp - currentLevelXp;
        
        double xpProgress = levelXpRange > 0 ? (double) progressXp / levelXpRange : 0.0;
        xpProgressBar.setProgress(xpProgress);
    }
    
    /**
     * Loads and displays the current flashcard question.
     */
    private void loadCurrentQuestion() {
        currentFlashcard = activeQuest.getCurrentFlashcard();
        
        if (currentFlashcard == null) {
            logger.error("No current flashcard available");
            appController.showErrorDialog("Error", "No question available!");
            return;
        }
        
        // Display question
        questionLabel.setText(currentFlashcard.getQuestion());
        categoryLabel.setText("📚 " + currentFlashcard.getCategory());
        
        // Display difficulty
        DifficultyLevel difficulty = currentFlashcard.getDifficulty();
        difficultyLabel.setText(difficulty.getDisplayText());
        
        // Reset UI state for new question
        resetQuestionState();
        
        logger.debug("Loaded question: {}", currentFlashcard.getQuestion());
    }
    
    /**
     * Resets the UI state for a new question.
     */
    private void resetQuestionState() {
        // Hide answer section
        answerSection.setVisible(false);
        answerSection.setManaged(false);
        
        // Show answer button, hide assessment buttons
        showAnswerButton.setVisible(true);
        showAnswerButton.setManaged(true);
        assessmentButtons.setVisible(false);
        assessmentButtons.setManaged(false);
        nextQuestionButton.setVisible(false);
        nextQuestionButton.setManaged(false);
        
        // Scroll to top
        Platform.runLater(() -> questionScrollPane.setVvalue(0));
    }
    
    /**
     * Handles showing the answer.
     */
    @FXML
    private void onShowAnswer() {
        logger.info("Showing answer for current question");
        
        // Display the answer
        answerLabel.setText(currentFlashcard.getAnswer());
        
        // Show answer section with fade effect
        answerSection.setVisible(true);
        answerSection.setManaged(true);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), answerSection);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
        
        // Hide show answer button, show assessment buttons
        showAnswerButton.setVisible(false);
        showAnswerButton.setManaged(false);
        assessmentButtons.setVisible(true);
        assessmentButtons.setManaged(true);
    }
    
    /**
     * Handles correct answer assessment.
     */
    @FXML
    private void onCorrect() {
        logger.info("Player marked answer as correct");
        processAnswer(true);
    }
    
    /**
     * Handles incorrect answer assessment.
     */
    @FXML
    private void onIncorrect() {
        logger.info("Player marked answer as incorrect");
        processAnswer(false);
    }
    
    /**
     * Processes the player's self-assessment and updates game state.
     */
    private void processAnswer(boolean correct) {
        try {
            // Process the answer through game service
            Quest.QuestionResult result = gameService.processQuestAnswer(correct);
            
            if (result.isError()) {
                appController.showErrorDialog("Error", "Failed to process answer!");
                return;
            }
            
            // Show XP feedback if earned
            if (result.getXpEarned() > 0) {
                showXpFeedback(result.getXpEarned(), correct);
            }
            
            // Check for quest completion or failure
            if (result.isQuestComplete()) {
                handleQuestCompletion();
            } else if (player.getCurrentHp() <= 0) {
                handleQuestFailure();
            } else {
                // Continue to next question
                prepareNextQuestion();
            }
            
        } catch (Exception e) {
            logger.error("Error processing answer", e);
            appController.showErrorDialog("Error", "Failed to process answer: " + e.getMessage());
        }
    }
    
    /**
     * Shows XP feedback animation.
     */
    private void showXpFeedback(int xpEarned, boolean correct) {
        // Update the quest XP display
        questXpEarned += xpEarned;
        questXpLabel.setText(questXpEarned + " XP");
        
        // Update player displays
        updateDisplay();
        
        // Show feedback (simple text update for now)
        String feedback = correct ? 
            String.format("✅ +%d XP!", xpEarned) : 
            "❌ No XP earned";
            
        // Could add animation here in the future
        logger.info("XP feedback: {}", feedback);
    }
    
    /**
     * Prepares UI for next question.
     */
    private void prepareNextQuestion() {
        // Hide assessment buttons, show next question button
        assessmentButtons.setVisible(false);
        assessmentButtons.setManaged(false);
        nextQuestionButton.setVisible(true);
        nextQuestionButton.setManaged(true);
        
        // Update display for any changes (HP, etc.)
        updateDisplay();
    }
    
    /**
     * Handles moving to the next question.
     */
    @FXML
    private void onNextQuestion() {
        logger.info("Moving to next question");
        updateDisplay();
        loadCurrentQuestion();
    }
    
    /**
     * Handles quest completion.
     */
    private void handleQuestCompletion() {
        logger.info("Quest completed!");
        
        boolean isPerfect = activeQuest.isPerfectQuest();
        int totalXp = activeQuest.getTotalXpEarned();
        int correctAnswers = activeQuest.getCorrectAnswers();
        int totalQuestions = activeQuest.getQuestFlashcards().size();
        
        // Check for level up
        int oldLevel = player.getCurrentLevel();
        boolean leveledUp = false;
        
        // The GameService should have already processed XP, but let's check
        if (player.getCurrentLevel() > oldLevel) {
            leveledUp = true;
        }
        
        // Build completion message
        StringBuilder message = new StringBuilder();
        message.append(String.format("🎉 Quest Complete!\n\n"));
        message.append(String.format("📊 Results:\n"));
        message.append(String.format("• Questions: %d/%d correct\n", correctAnswers, totalQuestions));
        message.append(String.format("• XP Earned: %d\n", totalXp));
        message.append(String.format("• HP Remaining: %d/%d\n", player.getCurrentHp(), player.getMaxHp()));
        
        if (isPerfect) {
            message.append("\n💎 Perfect Quest Bonus: +25 XP!");
        }
        
        if (leveledUp) {
            message.append(String.format("\n🆙 LEVEL UP! You are now level %d!", player.getCurrentLevel()));
        }
        
        // Check for new achievements
        var unlockedTitles = player.getUnlockedTitles();
        if (!unlockedTitles.isEmpty()) {
            message.append("\n\n🏆 Achievements:");
            for (String title : unlockedTitles) {
                message.append(String.format("\n• %s", title));
            }
        }
        
        appController.showInfoDialog("Quest Complete! 🎉", message.toString());
        appController.showMainMenu();
    }
    
    /**
     * Handles quest failure (HP reached zero).
     */
    private void handleQuestFailure() {
        logger.info("Quest failed - HP reached zero");
        
        int questionsAnswered = activeQuest.getCurrentQuestionIndex();
        int correctAnswers = activeQuest.getCorrectAnswers();
        
        String message = String.format(
            "💀 Quest Failed!\n\n" +
            "You ran out of health points.\n\n" +
            "📊 Progress:\n" +
            "• Questions answered: %d\n" +
            "• Correct answers: %d\n" +
            "• XP earned: %d\n\n" +
            "💡 Your health will be restored for the next quest!\n" +
            "Keep learning and try again!",
            questionsAnswered, correctAnswers, questXpEarned
        );
        
        appController.showErrorDialog("Quest Failed 💀", message);
        appController.showMainMenu();
    }
    
    /**
     * Handles quest abandonment.
     */
    @FXML
    private void onAbandonQuest() {
        logger.info("Player requested to abandon quest");
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Abandon Quest?");
        alert.setHeaderText("Are you sure you want to abandon this quest?");
        alert.setContentText("You will lose all progress and return to the main menu.\nYour XP and HP will remain unchanged.");
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.info("Quest abandoned by player");
            appController.showMainMenu();
        }
    }
}
