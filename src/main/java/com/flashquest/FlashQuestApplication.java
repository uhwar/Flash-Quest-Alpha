package com.flashquest;

import com.flashquest.ui.AppController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main JavaFX application class for FlashQuest.
 * Sets up the primary window and initializes the game through AppController.
 */
public class FlashQuestApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(FlashQuestApplication.class);
    
    // Window configuration - 720p optimized
    private static final String APP_TITLE = "FlashQuest - Gamified Learning";
    private static final int MIN_WIDTH = 1024;  // Minimum for 720p content
    private static final int MIN_HEIGHT = 600;   // Minimum usable height
    
    private AppController appController;

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting FlashQuest application...");
            
            // Configure the primary stage
            configurePrimaryStage(primaryStage);
            
            // Create and initialize the app controller
            appController = new AppController(primaryStage);
            appController.initialize();
            
            logger.info("FlashQuest application started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start FlashQuest application", e);
            showErrorAndExit("Failed to start application: " + e.getMessage());
        }
    }

    /**
     * Configures the primary stage window properties.
     */
    private void configurePrimaryStage(Stage primaryStage) {
        primaryStage.setTitle(APP_TITLE);
        
        // Remove Windows default border for true custom styling
        primaryStage.initStyle(StageStyle.UNDECORATED);
        
        // Set window size constraints (will be properly scaled by AppController)
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMaximized(false);
        primaryStage.setResizable(false); // Disable resize for clean custom border
        
        // Center the window on screen
        primaryStage.centerOnScreen();
        
        // Handle window close event
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Application closing...");
            if (appController != null) {
                appController.shutdown();
            }
        });
    }

    /**
     * Shows an error dialog and exits the application.
     */
    private void showErrorAndExit(String message) {
        // For now, just log and exit - we'll implement proper error dialogs later
        logger.error("Fatal error: " + message);
        System.err.println("FlashQuest Error: " + message);
        System.exit(1);
    }

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        try {
            logger.info("Launching FlashQuest...");
            launch(args);
        } catch (Exception e) {
            logger.error("Failed to launch FlashQuest", e);
            System.err.println("Failed to launch FlashQuest: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * JavaFX initialization - called before start().
     */
    @Override
    public void init() throws Exception {
        super.init();
        logger.info("Initializing FlashQuest application...");
        
        // TODO: Initialize services and load game data
        // This will be implemented when we add service layers
    }

    /**
     * JavaFX cleanup - called when application is shutting down.
     */
    @Override
    public void stop() throws Exception {
        logger.info("Stopping FlashQuest application...");
        
        if (appController != null) {
            appController.shutdown();
        }
        
        super.stop();
    }
}
