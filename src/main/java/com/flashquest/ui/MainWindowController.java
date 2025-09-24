package com.flashquest.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the main window wrapper that manages the custom title bar and content area.
 */
public class MainWindowController {
    private static final Logger logger = LoggerFactory.getLogger(MainWindowController.class);
    
    @FXML private VBox contentArea;
    @FXML private CustomTitleBarController titleBarController;
    
    private Stage stage;
    
    public void initialize() {
        logger.debug("Initializing MainWindowController");
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
        if (titleBarController != null) {
            titleBarController.setStage(stage);
        }
    }
    
    public void setTitle(String title) {
        if (titleBarController != null) {
            titleBarController.setTitle(title);
        }
    }
    
    public void setContent(Node content) {
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        }
    }
    
    public VBox getContentArea() {
        return contentArea;
    }
}