package com.flashquest.ui;

import com.flashquest.model.DifficultyLevel;
import com.flashquest.model.Flashcard;
import com.flashquest.service.FlashcardImportService;
import com.flashquest.service.GameService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the flashcard management screen.
 * Handles viewing, adding, editing, and importing flashcards.
 */
public class FlashcardManagerController implements Initializable, ScreenController {
    private static final Logger logger = LoggerFactory.getLogger(FlashcardManagerController.class);
    
    @FXML private Button backButton;
    @FXML private Button addCardButton;
    @FXML private Button importCardsButton;
    @FXML private Button openTemplateButton;
    
    @FXML private TableView<Flashcard> flashcardTable;
    @FXML private TableColumn<Flashcard, String> questionColumn;
    @FXML private TableColumn<Flashcard, String> categoryColumn;
    @FXML private TableColumn<Flashcard, String> difficultyColumn;
    @FXML private TableColumn<Flashcard, String> tagsColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<DifficultyLevel> difficultyFilterComboBox;
    
    @FXML private Label totalCardsLabel;
    @FXML private Label filteredCardsLabel;
    
    private AppController appController;
    private GameService gameService;
    private ObservableList<Flashcard> allFlashcards;
    private ObservableList<Flashcard> filteredFlashcards;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing FlashcardManagerController");
        gameService = GameService.getInstance();
        
        // Initialize collections first to avoid null pointer issues
        allFlashcards = FXCollections.observableArrayList();
        filteredFlashcards = FXCollections.observableArrayList();
        
        if (flashcardTable != null) {
            setupTableColumns();
        }
        if (categoryFilterComboBox != null && difficultyFilterComboBox != null) {
            setupFilters();
        }
    }

    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
        refreshFlashcards();
    }

    @Override
    public void onScreenShown() {
        refreshFlashcards();
    }

    /**
     * Sets up the table columns with proper cell value factories.
     */
    private void setupTableColumns() {
        if (questionColumn != null) {
            questionColumn.setCellValueFactory(new PropertyValueFactory<>("question"));
            questionColumn.setPrefWidth(300);
        }
        if (categoryColumn != null) {
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            categoryColumn.setPrefWidth(150);
        }
        if (difficultyColumn != null) {
            difficultyColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDifficulty().getDisplayName()));
            difficultyColumn.setPrefWidth(100);
        }
        if (tagsColumn != null) {
            // Tags not supported in current Flashcard model, show empty string
            tagsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));
            tagsColumn.setPrefWidth(200);
        }

        // Enable row selection
        flashcardTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Sets up the filter controls.
     */
    private void setupFilters() {
        // Category filter
        if (categoryFilterComboBox != null) {
            categoryFilterComboBox.setPromptText("All Categories");
            categoryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        // Difficulty filter
        if (difficultyFilterComboBox != null) {
            difficultyFilterComboBox.getItems().addAll(DifficultyLevel.values());
            difficultyFilterComboBox.setPromptText("All Difficulties");
            difficultyFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        // Search field
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            searchField.setPromptText("Search questions, answers, or tags...");
        }
    }

    /**
     * Refreshes the flashcard list from the game service.
     */
    private void refreshFlashcards() {
        List<Flashcard> cards = gameService.getAllFlashcards();
        allFlashcards.clear();
        allFlashcards.addAll(cards);
        
        filteredFlashcards.clear();
        filteredFlashcards.addAll(cards);
        
        if (flashcardTable != null) {
            flashcardTable.setItems(filteredFlashcards);
        }
        
        // Update category filter options
        if (categoryFilterComboBox != null) {
            List<String> categories = cards.stream()
                .map(Flashcard::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            String selectedCategory = categoryFilterComboBox.getValue();
            categoryFilterComboBox.getItems().clear();
            categoryFilterComboBox.getItems().addAll(categories);
            
            if (selectedCategory != null && categories.contains(selectedCategory)) {
                categoryFilterComboBox.setValue(selectedCategory);
            }
        }
        
        updateCountLabels();
        applyFilters();
    }

    /**
     * Applies the current filters to the flashcard list.
     */
    private void applyFilters() {
        if (allFlashcards == null) return;

        List<Flashcard> filtered = allFlashcards.stream()
            .filter(this::matchesFilters)
            .collect(Collectors.toList());

        filteredFlashcards.clear();
        filteredFlashcards.addAll(filtered);
        
        updateCountLabels();
    }

    /**
     * Checks if a flashcard matches the current filters.
     */
    private boolean matchesFilters(Flashcard card) {
        // Search text filter
        if (searchField != null) {
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String search = searchText.toLowerCase().trim();
                boolean matchesSearch = card.getQuestion().toLowerCase().contains(search) ||
                                      card.getAnswer().toLowerCase().contains(search) ||
                                      card.getCategory().toLowerCase().contains(search);
                                      // Note: Tags search removed since current Flashcard model doesn't support tags
                if (!matchesSearch) return false;
            }
        }

        // Category filter
        if (categoryFilterComboBox != null) {
            String selectedCategory = categoryFilterComboBox.getValue();
            if (selectedCategory != null && !selectedCategory.equals(card.getCategory())) {
                return false;
            }
        }

        // Difficulty filter
        if (difficultyFilterComboBox != null) {
            DifficultyLevel selectedDifficulty = difficultyFilterComboBox.getValue();
            if (selectedDifficulty != null && !selectedDifficulty.equals(card.getDifficulty())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Updates the count labels.
     */
    private void updateCountLabels() {
        int total = allFlashcards != null ? allFlashcards.size() : 0;
        int filtered = filteredFlashcards != null ? filteredFlashcards.size() : 0;
        
        if (totalCardsLabel != null) {
            totalCardsLabel.setText(String.valueOf(total));
        }
        if (filteredCardsLabel != null) {
            filteredCardsLabel.setText(String.valueOf(filtered));
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

    @FXML
    private void onBackToMenu() {
        onBack();
    }

    /**
     * Handles the add card button click.
     */
    @FXML
    private void onAddCard() {
        logger.info("Add card button clicked");
        
        // Show add card dialog
        AddCardDialog dialog = new AddCardDialog();
        dialog.showAndWait().ifPresent(card -> {
            gameService.addFlashcard(card.getQuestion(), card.getAnswer(), 
                                   card.getCategory(), card.getDifficulty());
            refreshFlashcards();
            if (appController != null) {
                appController.showInfoDialog("Card Added", 
                    "Flashcard added successfully!\n\nQuestion: " + 
                    truncateText(card.getQuestion(), 60));
            }
        });
    }

    /**
     * Handles the import cards button click.
     */
    @FXML
    private void onImportCards() {
        logger.info("Import cards button clicked");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Flashcards");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        // Set initial directory to user's home
        File initialDir = new File(System.getProperty("user.home"));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        
        File selectedFile = fileChooser.showOpenDialog(
            importCardsButton != null ? importCardsButton.getScene().getWindow() : null);
        
        if (selectedFile != null) {
            performImport(selectedFile);
        }
    }

    /**
     * Performs the actual import operation.
     */
    private void performImport(File file) {
        try {
            logger.info("Starting import from file: {}", file.getAbsolutePath());
            
            FlashcardImportService.ImportResult result = gameService.importFlashcardsFromFile(file);
            
            // Refresh the UI
            refreshFlashcards();
            
            // Show results dialog
            showImportResults(result, file.getName());
            
        } catch (Exception e) {
            logger.error("Error during import", e);
            if (appController != null) {
                appController.showErrorDialog("Import Error", 
                    "Failed to import flashcards:\n\n" + e.getMessage());
            }
        }
    }

    /**
     * Shows the import results dialog.
     */
    private void showImportResults(FlashcardImportService.ImportResult result, String fileName) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Import from '%s' completed!\n\n", fileName));
        message.append(String.format("📊 Results:\n"));
        message.append(String.format("• Total cards processed: %d\n", result.getTotalCards()));
        message.append(String.format("• Successfully imported: %d\n", result.getSuccessfulImports()));
        message.append(String.format("• Duplicates skipped: %d\n", result.getSkippedDuplicates()));
        message.append(String.format("• Failed imports: %d\n", result.getFailedImports()));

        if (!result.getErrorMessages().isEmpty()) {
            message.append("\n⚠️ Issues encountered:\n");
            int maxErrors = Math.min(5, result.getErrorMessages().size());
            for (int i = 0; i < maxErrors; i++) {
                message.append("• ").append(result.getErrorMessages().get(i)).append("\n");
            }
            if (result.getErrorMessages().size() > maxErrors) {
                message.append(String.format("... and %d more\n", 
                    result.getErrorMessages().size() - maxErrors));
            }
        }

        String title = result.isSuccessful() ? "Import Successful!" : "Import Completed with Issues";
        if (appController != null) {
            appController.showInfoDialog(title, message.toString());
        }
    }

    /**
     * Handles the open template button click.
     */
    @FXML
    private void onOpenTemplate() {
        logger.info("Open template button clicked");
        
        try {
            File templateFile = new File("Import_Template.txt");
            if (templateFile.exists()) {
                // Try to open with default text editor
                java.awt.Desktop.getDesktop().open(templateFile);
            } else {
                if (appController != null) {
                    appController.showErrorDialog("Template Not Found", 
                        "Import_Template.txt not found in the project directory.\n\n" +
                        "Please ensure the template file exists in:\n" + 
                        System.getProperty("user.dir"));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to open template file", e);
            if (appController != null) {
                appController.showErrorDialog("Cannot Open Template", 
                    "Failed to open the import template:\n\n" + e.getMessage() +
                    "\n\nPlease open Import_Template.txt manually from the project directory.");
            }
        }
    }

    /**
     * Truncates text to the specified length with ellipsis.
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Simple dialog for adding a new flashcard.
     */
    private static class AddCardDialog extends Dialog<Flashcard> {
        public AddCardDialog() {
            setTitle("Add New Flashcard");
            setHeaderText("Create a new flashcard");

            // Create form fields
            TextField questionField = new TextField();
            questionField.setPromptText("Enter the question...");
            
            TextArea answerArea = new TextArea();
            answerArea.setPromptText("Enter the answer...");
            answerArea.setPrefRowCount(3);
            
            TextField categoryField = new TextField();
            categoryField.setPromptText("Enter category...");
            
            ComboBox<DifficultyLevel> difficultyComboBox = new ComboBox<>();
            difficultyComboBox.getItems().addAll(DifficultyLevel.values());
            difficultyComboBox.setValue(DifficultyLevel.MEDIUM);

            // Create layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Question:"), 0, 0);
            grid.add(questionField, 1, 0);
            grid.add(new Label("Answer:"), 0, 1);
            grid.add(answerArea, 1, 1);
            grid.add(new Label("Category:"), 0, 2);
            grid.add(categoryField, 1, 2);
            grid.add(new Label("Difficulty:"), 0, 3);
            grid.add(difficultyComboBox, 1, 3);

            getDialogPane().setContent(grid);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Enable/disable OK button
            Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            questionField.textProperty().addListener((obs, old, text) -> 
                okButton.setDisable(text.trim().length() < 3));

            // Convert result
            setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return new Flashcard(
                        questionField.getText().trim(),
                        answerArea.getText().trim(),
                        categoryField.getText().trim(),
                        difficultyComboBox.getValue()
                    );
                }
                return null;
            });
        }
    }
}
