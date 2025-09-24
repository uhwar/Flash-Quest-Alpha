package com.flashquest.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Placeholder controller for the player statistics screen.
 * This will be replaced with full implementation in a future development phase.
 */
public class PlayerStatsController implements Initializable, ScreenController {
    private static final Logger logger = LoggerFactory.getLogger(PlayerStatsController.class);
    private AppController appController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing PlayerStatsController placeholder");
    }

    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void onBackToMenu() {
        logger.info("Back to menu from player stats screen");
        appController.showMainMenu();
    }
}