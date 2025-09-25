package com.flashquest.ui;

import com.flashquest.service.DataService;
import com.flashquest.service.GameService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the start screen that allows players to load existing saves or create new ones.
 */
public class StartScreenController implements ScreenController, Initializable {
    private static final Logger logger = LoggerFactory.getLogger(StartScreenController.class);
    
    @FXML private ListView<DataService.SaveInfo> saveListView;
    @FXML private Button loadGameButton;
    @FXML private Button deleteGameButton;
    @FXML private Button newGameButton;
    @FXML private Button settingsButton;
    @FXML private Button exitButton;
    
    private AppController appController;
    private GameService gameService;
    private DataService dataService;
    private ObservableList<DataService.SaveInfo> saveList;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.gameService = GameService.getInstance();
        this.dataService = new DataService();
        this.saveList = FXCollections.observableArrayList();
        
        setupSaveList();
        loadAvailableSaves();
    }
    
    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
    }
    
    /**
     * Sets up the save list view with custom cell factory for displaying save information.
     */
    private void setupSaveList() {
        saveListView.setItems(saveList);
        
        // Custom cell factory to display save information nicely
        saveListView.setCellFactory(listView -> new ListCell<DataService.SaveInfo>() {
            @Override
            protected void updateItem(DataService.SaveInfo saveInfo, boolean empty) {
                super.updateItem(saveInfo, empty);
                
                if (empty || saveInfo == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Format save information for display
                    String displayText = String.format(
                        "🏆 %s  •  Level %d  •  %,d XP  •  %d Quests\n📅 Last played: %s",
                        saveInfo.getPlayerName(),
                        saveInfo.getLevel(),
                        saveInfo.getTotalXp(),
                        saveInfo.getQuestsCompleted(),
                        saveInfo.getFormattedLastModified()
                    );
                    setText(displayText);
                    
                    // Style based on save condition
                    if (saveInfo.getPlayerName().equals("Corrupted Save")) {
                        setStyle("-fx-text-fill: #e74c3c;"); // Red for corrupted saves
                    } else {
                        setStyle("-fx-text-fill: #e8e8e8;"); // Normal color
                    }
                }
            }
        });
        
        // Handle selection changes
        saveListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            boolean isValidSave = hasSelection && !newSelection.getPlayerName().equals("Corrupted Save");
            
            loadGameButton.setDisable(!isValidSave);
            deleteGameButton.setDisable(!hasSelection);
        });
        
        // Handle double-click to load game
        saveListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && loadGameButton.isDisable() == false) {
                onLoadGame();
            }
        });
    }
    
    /**
     * Loads and displays available save files.
     */
    private void loadAvailableSaves() {
        try {
            List<DataService.SaveInfo> saves = dataService.getAvailableSaves();
            saveList.clear();
            saveList.addAll(saves);
            
            logger.info("Found {} save files", saves.size());
            
            // Auto-select the most recent save if available
            if (!saves.isEmpty() && !saves.get(0).getPlayerName().equals("Corrupted Save")) {
                saveListView.getSelectionModel().select(0);
            }
            
        } catch (Exception e) {
            logger.error("Failed to load available saves", e);
            appController.showErrorDialog("Error Loading Saves", 
                "Failed to load available save files: " + e.getMessage());
        }
    }
    
    /**
     * Handles loading the selected save game.
     */
    @FXML
    private void onLoadGame() {
        DataService.SaveInfo selectedSave = saveListView.getSelectionModel().getSelectedItem();
        if (selectedSave == null || selectedSave.getPlayerName().equals("Corrupted Save")) {
            appController.showErrorDialog("Invalid Selection", "Please select a valid save file to load.");
            return;
        }
        
        try {
            logger.info("Loading save: {}", selectedSave);
            
            // Initialize the game service without creating a new player
            gameService.initializeGame();
            
            if (gameService.hasPlayer()) {
                logger.info("Successfully loaded player: {}", gameService.getCurrentPlayer().getName());
                appController.showMainMenu();
            } else {
                appController.showErrorDialog("Load Failed", "Failed to load the selected save file.");
            }
            
        } catch (Exception e) {
            logger.error("Failed to load save: {}", selectedSave, e);
            appController.showErrorDialog("Load Error", 
                "Failed to load save file: " + e.getMessage());
        }
    }
    
    /**
     * Handles deleting the selected save game.
     */
    @FXML
    private void onDeleteGame() {
        DataService.SaveInfo selectedSave = saveListView.getSelectionModel().getSelectedItem();
        if (selectedSave == null) {
            return;
        }
        
        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Save File");
        confirmDialog.setContentText(String.format(
            "Are you sure you want to delete the save for '%s'?\n\nThis action cannot be undone.",
            selectedSave.getPlayerName()
        ));
        confirmDialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the save data
                dataService.deleteAllData();
                
                logger.info("Deleted save: {}", selectedSave);
                appController.showInfoDialog("Save Deleted", 
                    String.format("Save file for '%s' has been deleted.", selectedSave.getPlayerName()));
                
                // Refresh the save list
                loadAvailableSaves();
                
            } catch (Exception e) {
                logger.error("Failed to delete save: {}", selectedSave, e);
                appController.showErrorDialog("Delete Error", 
                    "Failed to delete save file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles creating a new game.
     */
    @FXML
    private void onNewGame() {
        try {
            logger.info("Starting new game creation");
            appController.showPlayerCreation();
            
        } catch (Exception e) {
            logger.error("Failed to start new game creation", e);
            appController.showErrorDialog("New Game Error", 
                "Failed to start new game creation: " + e.getMessage());
        }
    }
    
    /**
     * Handles opening settings (placeholder for future implementation).
     */
    @FXML
    private void onSettings() {
        appController.showInfoDialog("Settings", "Settings screen is not implemented yet.");
    }
    
    /**
     * Handles exiting the application.
     */
    @FXML
    private void onExit() {
        logger.info("User requested application exit");
        Platform.exit();
    }
}