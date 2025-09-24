package com.flashquest.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the custom title bar with window controls.
 */
public class CustomTitleBarController {
    private static final Logger logger = LoggerFactory.getLogger(CustomTitleBarController.class);
    
    @FXML private Label titleIcon;
    @FXML private Label titleLabel;
    @FXML private Button minimizeButton;
    @FXML private Button closeButton;
    
    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;
    
    public void initialize() {
        // Enable window dragging by clicking and dragging the title bar
        titleLabel.setOnMousePressed(this::onMousePressed);
        titleLabel.setOnMouseDragged(this::onMouseDragged);
        titleIcon.setOnMousePressed(this::onMousePressed);
        titleIcon.setOnMouseDragged(this::onMouseDragged);
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setTitle(String title) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
    }
    
    @FXML
    private void onMinimize() {
        if (stage != null) {
            logger.debug("Minimizing window");
            stage.setIconified(true);
        }
    }
    
    @FXML
    private void onClose() {
        if (stage != null) {
            logger.debug("Closing window");
            stage.close();
        }
    }
    
    private void onMousePressed(MouseEvent event) {
        if (stage != null) {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        }
    }
    
    private void onMouseDragged(MouseEvent event) {
        if (stage != null) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }
}