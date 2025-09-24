package com.flashquest.service;

import com.flashquest.model.DifficultyLevel;
import com.flashquest.model.Flashcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for importing flashcards from text files.
 * Supports the FlashQuest import format with comprehensive validation and error reporting.
 */
public class FlashcardImportService {
    private static final Logger logger = LoggerFactory.getLogger(FlashcardImportService.class);
    
    private static final String CARD_SEPARATOR = "---";
    private static final String QUESTION_PREFIX = "QUESTION:";
    private static final String ANSWER_PREFIX = "ANSWER:";
    private static final String DIFFICULTY_PREFIX = "DIFFICULTY:";
    private static final String CATEGORY_PREFIX = "CATEGORY:";
    private static final String TAGS_PREFIX = "TAGS:";
    
    private static final int MIN_QUESTION_LENGTH = 3;
    private static final int MIN_ANSWER_LENGTH = 1;
    private static final int MAX_CATEGORY_LENGTH = 50;
    private static final int MAX_TAG_LENGTH = 30;
    private static final int MAX_TAGS_PER_CARD = 20;

    /**
     * Result of an import operation containing success/failure statistics and details.
     */
    public static class ImportResult {
        private final int totalCards;
        private final int successfulImports;
        private final int skippedDuplicates;
        private final int failedImports;
        private final List<String> errorMessages;
        private final List<Flashcard> importedCards;

        public ImportResult(int totalCards, int successfulImports, int skippedDuplicates, 
                          int failedImports, List<String> errorMessages, List<Flashcard> importedCards) {
            this.totalCards = totalCards;
            this.successfulImports = successfulImports;
            this.skippedDuplicates = skippedDuplicates;
            this.failedImports = failedImports;
            this.errorMessages = new ArrayList<>(errorMessages);
            this.importedCards = new ArrayList<>(importedCards);
        }

        // Getters
        public int getTotalCards() { return totalCards; }
        public int getSuccessfulImports() { return successfulImports; }
        public int getSkippedDuplicates() { return skippedDuplicates; }
        public int getFailedImports() { return failedImports; }
        public List<String> getErrorMessages() { return new ArrayList<>(errorMessages); }
        public List<Flashcard> getImportedCards() { return new ArrayList<>(importedCards); }

        public boolean isSuccessful() {
            return successfulImports > 0 || (totalCards == 0 && failedImports == 0);
        }

        public String getSummary() {
            return String.format("Import Results: %d total, %d imported, %d duplicates skipped, %d failed",
                totalCards, successfulImports, skippedDuplicates, failedImports);
        }
    }

    /**
     * Imports flashcards from a text file.
     * 
     * @param file The text file to import from
     * @param existingCards Set of existing flashcard questions to check for duplicates
     * @return ImportResult containing statistics and imported cards
     */
    public ImportResult importFromFile(File file, Set<String> existingCards) {
        logger.info("Starting import from file: {}", file.getAbsolutePath());
        
        List<String> errorMessages = new ArrayList<>();
        List<Flashcard> importedCards = new ArrayList<>();
        int successfulImports = 0;
        int skippedDuplicates = 0;
        int failedImports = 0;

        try {
            String content = readFileContent(file);
            List<String> cardSections = parseCardSections(content);
            
            logger.info("Found {} potential flashcards in file", cardSections.size());

            for (int i = 0; i < cardSections.size(); i++) {
                String section = cardSections.get(i);
                int cardNumber = i + 1;
                
                try {
                    Flashcard card = parseFlashcard(section, cardNumber);
                    
                    // Check for duplicate
                    if (existingCards.contains(card.getQuestion().toLowerCase().trim())) {
                        logger.debug("Skipping duplicate card {}: {}", cardNumber, 
                            truncateText(card.getQuestion(), 50));
                        skippedDuplicates++;
                        errorMessages.add(String.format("Card %d: Duplicate question skipped - \"%s\"", 
                            cardNumber, truncateText(card.getQuestion(), 50)));
                        continue;
                    }
                    
                    // Add to results
                    importedCards.add(card);
                    existingCards.add(card.getQuestion().toLowerCase().trim());
                    successfulImports++;
                    
                    logger.debug("Successfully imported card {}: {}", cardNumber, 
                        truncateText(card.getQuestion(), 50));
                        
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = String.format("Card %d: %s", cardNumber, e.getMessage());
                    errorMessages.add(errorMsg);
                    logger.warn("Failed to import card {}: {}", cardNumber, e.getMessage());
                }
            }

        } catch (IOException e) {
            failedImports++;
            String errorMsg = "Failed to read file: " + e.getMessage();
            errorMessages.add(errorMsg);
            logger.error("IOException while importing from file: {}", file.getAbsolutePath(), e);
        }

        int totalCards = successfulImports + skippedDuplicates + failedImports;
        ImportResult result = new ImportResult(totalCards, successfulImports, skippedDuplicates, 
                                             failedImports, errorMessages, importedCards);
        
        logger.info("Import completed: {}", result.getSummary());
        return result;
    }

    /**
     * Reads the entire content of a file as UTF-8 text.
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }

    /**
     * Parses the file content into individual card sections separated by "---".
     */
    private List<String> parseCardSections(String content) {
        List<String> sections = new ArrayList<>();
        
        String[] parts = content.split("(?m)^" + CARD_SEPARATOR + "\\s*$");
        
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                sections.add(trimmed);
            }
        }
        
        return sections;
    }

    /**
     * Parses a single flashcard section into a Flashcard object.
     */
    private Flashcard parseFlashcard(String section, int cardNumber) throws Exception {
        String[] lines = section.split("\n");
        
        StringBuilder currentContent = new StringBuilder();
        String currentField = null;
        
        String question = null;
        String answer = null;
        String difficulty = null;
        String category = null;
        String tags = null;

        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Skip empty lines
            if (trimmedLine.isEmpty()) {
                if (currentField != null) {
                    currentContent.append("\n");
                }
                continue;
            }

            // Check if this line starts a new field
            String newField = null;
            if (trimmedLine.startsWith(QUESTION_PREFIX)) {
                newField = "QUESTION";
                trimmedLine = trimmedLine.substring(QUESTION_PREFIX.length()).trim();
            } else if (trimmedLine.startsWith(ANSWER_PREFIX)) {
                newField = "ANSWER";
                trimmedLine = trimmedLine.substring(ANSWER_PREFIX.length()).trim();
            } else if (trimmedLine.startsWith(DIFFICULTY_PREFIX)) {
                newField = "DIFFICULTY";
                trimmedLine = trimmedLine.substring(DIFFICULTY_PREFIX.length()).trim();
            } else if (trimmedLine.startsWith(CATEGORY_PREFIX)) {
                newField = "CATEGORY";
                trimmedLine = trimmedLine.substring(CATEGORY_PREFIX.length()).trim();
            } else if (trimmedLine.startsWith(TAGS_PREFIX)) {
                newField = "TAGS";
                trimmedLine = trimmedLine.substring(TAGS_PREFIX.length()).trim();
            }

            // Save previous field content
            if (newField != null && currentField != null) {
                saveFieldContent(currentField, currentContent.toString().trim(), 
                               cardNumber, question, answer, difficulty, category, tags);
                
                // Update references based on current field
                switch (currentField) {
                    case "QUESTION": question = currentContent.toString().trim(); break;
                    case "ANSWER": answer = currentContent.toString().trim(); break;
                    case "DIFFICULTY": difficulty = currentContent.toString().trim(); break;
                    case "CATEGORY": category = currentContent.toString().trim(); break;
                    case "TAGS": tags = currentContent.toString().trim(); break;
                }
                
                currentContent.setLength(0);
            }

            // Start new field or continue current field
            if (newField != null) {
                currentField = newField;
                if (!trimmedLine.isEmpty()) {
                    currentContent.append(trimmedLine);
                }
            } else if (currentField != null) {
                if (currentContent.length() > 0) {
                    currentContent.append("\n");
                }
                currentContent.append(trimmedLine);
            }
        }

        // Save the last field
        if (currentField != null) {
            switch (currentField) {
                case "QUESTION": question = currentContent.toString().trim(); break;
                case "ANSWER": answer = currentContent.toString().trim(); break;
                case "DIFFICULTY": difficulty = currentContent.toString().trim(); break;
                case "CATEGORY": category = currentContent.toString().trim(); break;
                case "TAGS": tags = currentContent.toString().trim(); break;
            }
        }

        // Validate required fields
        validateFlashcardFields(question, answer, difficulty, category, cardNumber);

        // Parse difficulty level
        DifficultyLevel difficultyLevel;
        try {
            difficultyLevel = DifficultyLevel.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid difficulty '{}' for card {}, defaulting to MEDIUM", difficulty, cardNumber);
            difficultyLevel = DifficultyLevel.MEDIUM;
        }

        // Parse tags
        List<String> tagList = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            tagList = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .limit(MAX_TAGS_PER_CARD)
                    .collect(Collectors.toList());

            // Validate tag lengths
            tagList = tagList.stream()
                    .map(tag -> tag.length() > MAX_TAG_LENGTH ? 
                         tag.substring(0, MAX_TAG_LENGTH) : tag)
                    .collect(Collectors.toList());
        }

        // Validate and truncate category if necessary
        if (category.length() > MAX_CATEGORY_LENGTH) {
            category = category.substring(0, MAX_CATEGORY_LENGTH);
            logger.warn("Category truncated to {} characters for card {}", MAX_CATEGORY_LENGTH, cardNumber);
        }

        // Create and return the flashcard (note: current Flashcard model doesn't support tags)
        // For now, we'll ignore tags and create a basic flashcard
        return new Flashcard(question, answer, category, difficultyLevel);
    }

    /**
     * Helper method to save field content (placeholder for future use).
     */
    private void saveFieldContent(String field, String content, int cardNumber, 
                                String question, String answer, String difficulty, 
                                String category, String tags) {
        // This method is currently unused but kept for potential future enhancements
    }

    /**
     * Validates that all required fields are present and meet minimum requirements.
     */
    private void validateFlashcardFields(String question, String answer, String difficulty, 
                                       String category, int cardNumber) throws Exception {
        
        if (question == null || question.trim().length() < MIN_QUESTION_LENGTH) {
            throw new Exception(String.format("Question is missing or too short (minimum %d characters)", 
                MIN_QUESTION_LENGTH));
        }

        if (answer == null || answer.trim().length() < MIN_ANSWER_LENGTH) {
            throw new Exception(String.format("Answer is missing or too short (minimum %d characters)", 
                MIN_ANSWER_LENGTH));
        }

        if (difficulty == null || difficulty.trim().isEmpty()) {
            throw new Exception("Difficulty level is missing");
        }

        if (category == null || category.trim().isEmpty()) {
            throw new Exception("Category is missing");
        }
    }

    /**
     * Truncates text to specified length with ellipsis.
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}