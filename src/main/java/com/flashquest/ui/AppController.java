package com.flashquest.ui;

import com.flashquest.service.GameService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Main application controller managing screen navigation and app lifecycle.
 */
public class AppController {
    private static final Logger logger = LoggerFactory.getLogger(AppController.class);
    
    private Stage primaryStage;
    private Scene currentScene;
    private GameService gameService;
    
    // Screen dimensions - Base logical size (will be scaled automatically)
    private static final int BASE_WINDOW_WIDTH = 1280;
    private static final int BASE_WINDOW_HEIGHT = 720;
    
    public AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameService = GameService.getInstance();
    }
    
    /**
     * Initializes the application and shows the start screen.
     */
    public void initialize() {
        try {
            logger.info("Initializing FlashQuest application");
            
            // Always show start screen first - it will handle save loading
            showStartScreen();
            
        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
            showErrorDialog("Initialization Error", 
                "Failed to initialize FlashQuest: " + e.getMessage());
        }
    }
    
    /**
     * Shows the player creation dialog for new games.
     */
    public void showPlayerCreation() {
        try {
            // Clear any existing save data for new game
            if (gameService.hasPlayer()) {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("New Game");
                confirmDialog.setHeaderText("Create New Save");
                confirmDialog.setContentText("This will overwrite your existing save. Continue?");
                confirmDialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/styles/dark-theme.css").toExternalForm());
                
                Optional<ButtonType> confirmResult = confirmDialog.showAndWait();
                if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
                    showStartScreen(); // Return to start screen
                    return;
                }
            }
            
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Welcome to FlashQuest! 🎮");
            dialog.setHeaderText("Create Your Hero");
            dialog.setContentText("Enter your player name:");
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/dark-theme.css").toExternalForm());
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresentOrElse(
                name -> {
                    if (name.trim().isEmpty()) {
                        showErrorDialog("Invalid Name", "Player name cannot be empty!");
                        showPlayerCreation(); // Try again
                    } else {
                        // Clear existing save data and create new player
                        try {
                            if (gameService.hasPlayer()) {
                                gameService.createBackup(); // Backup before clearing
                                gameService.deleteAllData(); // Clear existing data
                            }
                            
                            gameService.initializeGame(); // Reinitialize
                            gameService.createNewPlayer(name);
                            showMainMenu();
                            
                        } catch (Exception e) {
                            logger.error("Failed to create new player", e);
                            showErrorDialog("Error", "Failed to create new player: " + e.getMessage());
                            showStartScreen();
                        }
                    }
                },
                () -> {
                    // User cancelled - return to start screen
                    showStartScreen();
                }
            );
            
        } catch (Exception e) {
            logger.error("Error in player creation", e);
            showErrorDialog("Error", "Failed to create player: " + e.getMessage());
            showStartScreen();
        }
    }
    
    /**
     * Shows the start screen with save selection.
     */
    public void showStartScreen() {
        loadScreen("/fxml/StartScreen.fxml", "FlashQuest - Welcome");
    }
    
    /**
     * Shows the main menu screen.
     */
    public void showMainMenu() {
        loadScreen("/fxml/MainMenu.fxml", "FlashQuest - Main Menu");
    }
    
    /**
     * Shows the quest selection screen.
     */
    public void showQuestSelection() {
        loadScreen("/fxml/QuestSelection.fxml", "FlashQuest - Select Quest");
    }
    
    /**
     * Shows the active quest screen.
     */
    public void showQuest() {
        loadScreen("/fxml/Quest.fxml", "FlashQuest - Quest in Progress");
    }
    
    /**
     * Shows the player statistics screen.
     */
    public void showPlayerStats() {
        loadScreen("/fxml/PlayerStats.fxml", "FlashQuest - Player Statistics");
    }
    
    /**
     * Shows the flashcard management screen.
     */
    public void showFlashcardManager() {
        loadScreen("/fxml/FlashcardManager.fxml", "FlashQuest - Manage Flashcards");
    }
    
    /**
     * Generic screen loader with error handling.
     */
    private void loadScreen(String fxmlPath, String title) {
        try {
            logger.debug("Loading screen: {}", fxmlPath);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Pass this controller to the loaded controller if it implements ScreenController
            Object controller = loader.getController();
            if (controller instanceof ScreenController) {
                ((ScreenController) controller).setAppController(this);
            }
            
            // Calculate optimal window size for this system
            double[] optimalSize = calculateOptimalWindowSize();
            Scene scene = new Scene(root, optimalSize[0], optimalSize[1]);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
            
            // Apply universal scaling
            applyUniversalScaling(scene);
            
            // Add window dragging support for undecorated stage
            addWindowDragSupport(root, scene);
            
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
            
            this.currentScene = scene;
            
        } catch (IOException e) {
            logger.error("Failed to load screen: {}", fxmlPath, e);
            
            // Fallback to a simple error screen
            showErrorDialog("Screen Loading Error", 
                "Failed to load " + fxmlPath + ": " + e.getMessage());
        }
    }
    
    /**
     * Shows an error dialog to the user.
     */
    public void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        alert.showAndWait();
    }
    
    /**
     * Shows an information dialog to the user.
     */
    public void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        alert.showAndWait();
    }
    
    /**
     * Saves game data and performs cleanup on application shutdown.
     */
    public void shutdown() {
        try {
            logger.info("Shutting down FlashQuest application");
            gameService.saveGameData();
        } catch (Exception e) {
            logger.error("Error during application shutdown", e);
        }
    }
    
    /**
     * Calculates optimal window size based on screen resolution and DPI
     */
    private double[] calculateOptimalWindowSize() {
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            java.awt.Dimension screenSize = toolkit.getScreenSize();
            int dpi = toolkit.getScreenResolution();
            
            double screenWidth = screenSize.getWidth();
            double screenHeight = screenSize.getHeight();
            double dpiScale = dpi / 96.0;
            
            logger.info("Screen: {}x{}, DPI: {}, Scale: {}x", 
                (int)screenWidth, (int)screenHeight, dpi, String.format("%.1f", dpiScale));
            
            // Calculate window size as percentage of screen, accounting for DPI
            double windowWidth, windowHeight;
            
            if (screenWidth >= 3840) { // 4K or higher
                windowWidth = screenWidth * 0.5;   // 50% of screen width
                windowHeight = screenHeight * 0.6; // 60% of screen height
            } else if (screenWidth >= 2560) { // 1440p
                windowWidth = screenWidth * 0.6;   // 60% of screen width
                windowHeight = screenHeight * 0.7; // 70% of screen height
            } else if (screenWidth >= 1920) { // 1080p
                windowWidth = screenWidth * 0.7;   // 70% of screen width
                windowHeight = screenHeight * 0.75; // 75% of screen height
            } else { // Lower resolutions
                windowWidth = Math.min(screenWidth * 0.9, BASE_WINDOW_WIDTH);
                windowHeight = Math.min(screenHeight * 0.8, BASE_WINDOW_HEIGHT);
            }
            
            // Ensure minimum usable size
            windowWidth = Math.max(windowWidth, 1024);
            windowHeight = Math.max(windowHeight, 600);
            
            // Don't exceed screen size
            windowWidth = Math.min(windowWidth, screenWidth * 0.95);
            windowHeight = Math.min(windowHeight, screenHeight * 0.9);
            
            logger.info("Calculated window size: {}x{}", (int)windowWidth, (int)windowHeight);
            
            return new double[]{windowWidth, windowHeight};
            
        } catch (Exception e) {
            logger.warn("Could not calculate optimal window size, using defaults", e);
            return new double[]{BASE_WINDOW_WIDTH, BASE_WINDOW_HEIGHT};
        }
    }
    
    /**
     * Applies universal scaling that works across all PC configurations
     */
    private void applyUniversalScaling(Scene scene) {
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            java.awt.Dimension screenSize = toolkit.getScreenSize();
            int dpi = toolkit.getScreenResolution();
            
            double screenWidth = screenSize.getWidth();
            double dpiScale = dpi / 96.0;
            
            // Calculate universal scale factor considering both resolution and DPI
            double universalScale = calculateUniversalScale(screenWidth, dpiScale);
            
            logger.info("Applying universal scale: {}x (Screen: {}, DPI Scale: {}x)", 
                String.format("%.2f", universalScale), (int)screenWidth, String.format("%.1f", dpiScale));
            
            // Apply appropriate CSS class based on universal scale
            String scaleClass = getScaleClass(universalScale);
            if (scaleClass != null) {
                scene.getRoot().getStyleClass().add(scaleClass);
                logger.info("Applied CSS class: {}", scaleClass);
            }
            
            // Apply root font scaling
            double baseFontSize = 14.0;
            double scaledFontSize = baseFontSize * universalScale;
            String fontStyle = String.format("-fx-font-size: %.1fpx;", scaledFontSize);
            scene.getRoot().setStyle(fontStyle);
            
        } catch (Exception e) {
            logger.warn("Could not apply universal scaling, using defaults", e);
        }
    }
    
    /**
     * Calculates universal scale factor that works across all configurations
     */
    private double calculateUniversalScale(double screenWidth, double dpiScale) {
        double baseScale;
        
        if (screenWidth >= 3840) { // 4K (3840x2160) or higher
            baseScale = 1.4;  // Larger UI for 4K screens
        } else if (screenWidth >= 2560) { // 1440p (2560x1440)
            baseScale = 1.2;  // Medium scaling for 1440p
        } else if (screenWidth >= 1920) { // 1080p (1920x1080)
            baseScale = 1.0;  // Standard scaling for 1080p
        } else if (screenWidth >= 1366) { // 768p (1366x768)
            baseScale = 0.9;  // Slightly smaller for lower resolutions
        } else { // Very low resolutions
            baseScale = 0.8;  // Compact UI for small screens
        }
        
        // Apply DPI scaling on top of resolution-based scaling
        double finalScale = baseScale * Math.max(1.0, dpiScale * 0.8); // Moderate DPI influence
        
        // Clamp to reasonable range
        return Math.max(0.8, Math.min(2.0, finalScale));
    }
    
    /**
     * Gets appropriate CSS class based on scale factor
     */
    private String getScaleClass(double scale) {
        if (scale >= 1.6) {
            return "scale-xlarge";  // Extra large scaling
        } else if (scale >= 1.3) {
            return "scale-large";   // Large scaling
        } else if (scale >= 1.1) {
            return "scale-medium";  // Medium scaling
        } else if (scale <= 0.9) {
            return "scale-small";   // Small scaling
        }
        return null; // Use default scaling
    }
    
    /**
     * Gets the system DPI scale factor
     */
    private double getSystemDPIScaleFactor() {
        try {
            // Try to get DPI from AWT Toolkit
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            int dpi = toolkit.getScreenResolution();
            
            // Calculate scaling factor (96 DPI is 100% scaling)
            double scaleFactor = dpi / 96.0;
            
            // Also check system properties for override
            String scaleProperty = System.getProperty("glass.win.uiScale");
            if (scaleProperty != null && scaleProperty.endsWith("%")) {
                try {
                    int scalePercent = Integer.parseInt(scaleProperty.replace("%", ""));
                    scaleFactor = Math.max(scaleFactor, scalePercent / 100.0);
                } catch (NumberFormatException e) {
                    // Ignore invalid scale property
                }
            }
            
            return scaleFactor;
        } catch (Exception e) {
            logger.warn("Could not detect system DPI, defaulting to 2.0x scaling", e);
            return 2.0; // Default to 200% scaling for safety
        }
    }
    
    /**
     * Adds window dragging support for undecorated stage
     */
    private void addWindowDragSupport(Parent root, Scene scene) {
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        
        // Enable dragging by clicking and dragging anywhere on the window
        root.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset[0]);
            primaryStage.setY(event.getScreenY() - yOffset[0]);
        });
        
        // Add ESC key to close window
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    primaryStage.close();
                    break;
            }
        });
    }
    
    // Getters
    public Stage getPrimaryStage() { return primaryStage; }
    public Scene getCurrentScene() { return currentScene; }
    public GameService getGameService() { return gameService; }
}
