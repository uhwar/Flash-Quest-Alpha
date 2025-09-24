package com.flashquest.ui;

import com.flashquest.model.Player;
import com.flashquest.service.DatabaseService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for save profile selection and management.
 * Allows players to create, select, and manage multiple game saves.
 */
public class SaveProfileController implements Initializable, ScreenController {
    private static final Logger logger = LoggerFactory.getLogger(SaveProfileController.class);
    
    @FXML private TableView<DatabaseService.SaveProfile> profileTable;
    @FXML private TableColumn<DatabaseService.SaveProfile, String> nameColumn;
    @FXML private TableColumn<DatabaseService.SaveProfile, String> playersColumn;
    @FXML private TableColumn<DatabaseService.SaveProfile, String> flashcardsColumn;
    @FXML private TableColumn<DatabaseService.SaveProfile, String> lastAccessedColumn;
    @FXML private TableColumn<DatabaseService.SaveProfile, String> descriptionColumn;
    
    @FXML private Button selectProfileButton;
    @FXML private Button newProfileButton;
    @FXML private Button deleteProfileButton;
    @FXML private Button refreshButton;
    @FXML private Button exitButton;
    
    @FXML private Label selectedProfileLabel;
    @FXML private TextArea selectedProfileDescription;
    
    private AppController appController;
    private DatabaseService databaseService;
    private ObservableList<DatabaseService.SaveProfile> profiles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing SaveProfileController");
        
        databaseService = DatabaseService.getInstance();
        profiles = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupEventHandlers();
        loadProfiles();
    }

    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @Override
    public void onScreenShown() {
        loadProfiles();
    }

    /**
     * Sets up the table columns with proper cell value factories.
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("profileName"));
        
        playersColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getPlayerCount())));
        
        flashcardsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getFlashcardCount())));
        
        lastAccessedColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getLastAccessed().format(formatter));
        });
        
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Set column widths
        nameColumn.setPrefWidth(150);
        playersColumn.setPrefWidth(80);
        flashcardsColumn.setPrefWidth(100);
        lastAccessedColumn.setPrefWidth(140);
        descriptionColumn.setPrefWidth(200);

        profileTable.setItems(profiles);
    }

    /**
     * Sets up event handlers for UI interactions.
     */
    private void setupEventHandlers() {
        // Enable/disable buttons based on selection
        profileTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> updateButtonStates(newSelection));

        // Double-click to select profile
        profileTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !profileTable.getSelectionModel().isEmpty()) {
                onSelectProfile();
            }
        });

        // Update description when selection changes
        profileTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> updateProfileDetails(newSelection));

        // Initially disable action buttons
        updateButtonStates(null);
    }

    /**
     * Loads all save profiles from the database.
     */
    private void loadProfiles() {
        try {
            List<DatabaseService.SaveProfile> profileList = databaseService.getAllSaveProfiles();
            profiles.clear();
            profiles.addAll(profileList);
            
            logger.info("Loaded {} save profiles", profileList.size());
            
        } catch (Exception e) {
            logger.error("Error loading save profiles", e);
            if (appController != null) {
                appController.showErrorDialog("Database Error", 
                    "Failed to load save profiles:\n\n" + e.getMessage());
            }
        }
    }

    /**
     * Updates button enabled states based on selection.
     */
    private void updateButtonStates(DatabaseService.SaveProfile selectedProfile) {
        boolean hasSelection = selectedProfile != null;
        selectProfileButton.setDisable(!hasSelection);
        deleteProfileButton.setDisable(!hasSelection || selectedProfile.getPlayerCount() > 0);
    }

    /**
     * Updates the profile details display.
     */
    private void updateProfileDetails(DatabaseService.SaveProfile profile) {
        if (profile != null) {
            selectedProfileLabel.setText(profile.getProfileName());
            selectedProfileDescription.setText(profile.getDescription() != null ? 
                profile.getDescription() : "No description");
        } else {
            selectedProfileLabel.setText("No profile selected");
            selectedProfileDescription.setText("");
        }
    }

    /**
     * Handles the select profile button click.
     */
    @FXML
    private void onSelectProfile() {
        DatabaseService.SaveProfile selected = profileTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        logger.info("Selected save profile: {}", selected.getProfileName());

        try {
            // Get players in this profile
            List<Player> players = databaseService.getPlayersInProfile(selected.getId());
            
            if (players.isEmpty()) {
                // No players in profile - show player creation dialog
                showPlayerCreationDialog(selected);
            } else {
                // Show player selection dialog
                showPlayerSelectionDialog(selected, players);
            }
            
        } catch (Exception e) {
            logger.error("Error selecting profile", e);
            appController.showErrorDialog("Error", "Failed to load profile: " + e.getMessage());
        }
    }

    /**
     * Shows a dialog to create a new player in the selected profile.
     */
    private void showPlayerCreationDialog(DatabaseService.SaveProfile profile) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Player");
        dialog.setHeaderText("Create a new player in profile: " + profile.getProfileName());

        TextField nameField = new TextField();
        nameField.setPromptText("Enter player name...");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Player Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Enable/disable OK button based on input
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, text) -> 
            okButton.setDisable(text.trim().length() < 2));

        dialog.setResultConverter(buttonType -> 
            buttonType == ButtonType.OK ? nameField.getText().trim() : null);

        dialog.showAndWait().ifPresent(playerName -> {
            try {
                // Create new player in profile
                Player newPlayer = databaseService.createPlayer(profile.getId(), playerName);
                
                // Initialize game with this profile and player
                initializeGameWithProfile(profile, newPlayer);
                
            } catch (Exception e) {
                logger.error("Error creating player", e);
                appController.showErrorDialog("Error", "Failed to create player: " + e.getMessage());
            }
        });
    }

    /**
     * Shows a dialog to select from existing players in the profile.
     */
    private void showPlayerSelectionDialog(DatabaseService.SaveProfile profile, List<Player> players) {
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle("Select Player");
        dialog.setHeaderText("Choose a player from profile: " + profile.getProfileName());

        ListView<Player> playerList = new ListView<>();
        playerList.getItems().addAll(players);
        playerList.setCellFactory(listView -> new ListCell<Player>() {
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                if (empty || player == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (Level %d, %d XP)", 
                        player.getName(), player.getCurrentLevel(), player.getTotalXp()));
                }
            }
        });

        // Select first player by default
        if (!players.isEmpty()) {
            playerList.getSelectionModel().selectFirst();
        }

        Button newPlayerButton = new Button("Create New Player");
        newPlayerButton.setOnAction(e -> {
            dialog.close();
            showPlayerCreationDialog(profile);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Select Player:"), 0, 0);
        grid.add(playerList, 0, 1, 2, 1);
        grid.add(newPlayerButton, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> 
            buttonType == ButtonType.OK ? playerList.getSelectionModel().getSelectedItem() : null);

        dialog.showAndWait().ifPresent(selectedPlayer -> {
            // Initialize game with selected profile and player
            initializeGameWithProfile(profile, selectedPlayer);
        });
    }

    /**
     * Initializes the game with the selected profile and player.
     */
    private void initializeGameWithProfile(DatabaseService.SaveProfile profile, Player player) {
        // TODO: Update GameService to work with database
        // For now, we'll continue with the existing system but log the selection
        logger.info("Initializing game with profile: {} and player: {}", 
            profile.getProfileName(), player.getName());

        // Close this dialog and proceed to main menu
        if (appController != null) {
            appController.showMainMenu();
        }
    }

    /**
     * Handles the new profile button click.
     */
    @FXML
    private void onNewProfile() {
        logger.info("New profile button clicked");

        Dialog<ProfileData> dialog = new Dialog<>();
        dialog.setTitle("Create New Save Profile");
        dialog.setHeaderText("Create a new FlashQuest save profile");

        TextField nameField = new TextField();
        nameField.setPromptText("Profile name...");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Optional description...");
        descriptionArea.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Profile Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, text) -> 
            okButton.setDisable(text.trim().length() < 2));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new ProfileData(nameField.getText().trim(), descriptionArea.getText().trim());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(profileData -> {
            try {
                String profileId = databaseService.createSaveProfile(
                    profileData.name, 
                    profileData.description.isEmpty() ? null : profileData.description
                );
                
                loadProfiles(); // Refresh the list
                
                appController.showInfoDialog("Profile Created", 
                    "New save profile '" + profileData.name + "' created successfully!");
                
            } catch (Exception e) {
                logger.error("Error creating save profile", e);
                appController.showErrorDialog("Error", "Failed to create profile: " + e.getMessage());
            }
        });
    }

    /**
     * Handles the delete profile button click.
     */
    @FXML
    private void onDeleteProfile() {
        DatabaseService.SaveProfile selected = profileTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        logger.info("Delete profile button clicked for: {}", selected.getProfileName());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Save Profile");
        alert.setHeaderText("Are you sure you want to delete this save profile?");
        alert.setContentText("Profile: " + selected.getProfileName() + 
                           "\n\nThis action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Implement profile deletion in DatabaseService
                appController.showInfoDialog("Not Implemented", 
                    "Profile deletion will be implemented in a future update.");
            }
        });
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void onRefresh() {
        logger.info("Refresh button clicked");
        loadProfiles();
    }

    /**
     * Handles the exit button click.
     */
    @FXML
    private void onExit() {
        logger.info("Exit button clicked");
        System.exit(0);
    }

    /**
     * Data class for profile creation.
     */
    private static class ProfileData {
        final String name;
        final String description;

        ProfileData(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}