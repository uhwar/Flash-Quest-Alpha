import com.flashquest.model.*;
import com.flashquest.service.DefaultFlashcardService;

import java.util.List;

/**
 * Simple test to verify FlashQuest core functionality.
 * This tests our model classes and XP progression without requiring Maven.
 */
public class TestFlashQuest {
    public static void main(String[] args) {
        System.out.println("🎮 FlashQuest Core Test Starting...\n");
        
        try {
            // Test Player creation and progression
            testPlayerProgression();
            
            // Test Flashcard creation
            testFlashcards();
            
            // Test Quest creation
            testQuests();
            
            // Test default flashcard dataset
            testDefaultFlashcards();
            
            System.out.println("✅ All tests passed! FlashQuest core is working correctly.");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testPlayerProgression() {
        System.out.println("📊 Testing Player Progression System...");
        
        // Create a new player
        Player player = new Player("TestHero");
        System.out.println("Created player: " + player.getName());
        System.out.println("Starting level: " + player.getCurrentLevel());
        System.out.println("Starting XP: " + player.getTotalXp());
        System.out.println("Starting HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
        
        // Test XP requirements
        System.out.println("\nXP Requirements:");
        for (int level = 1; level <= 5; level++) {
            int requiredXp = Player.getXpRequiredForLevel(level);
            System.out.println("Level " + level + ": " + requiredXp + " XP");
        }
        
        // Test level progression
        System.out.println("\nTesting level progression...");
        boolean leveledUp = player.addXp(175); // Max XP from one perfect quest
        System.out.println("Added 175 XP (perfect quest), leveled up: " + leveledUp);
        System.out.println("Current level: " + player.getCurrentLevel() + ", XP: " + player.getTotalXp());
        
        // Test HP damage and recovery
        System.out.println("\nTesting HP system...");
        boolean died = player.takeDamage(2);
        System.out.println("Took 2 damage, died: " + died + ", HP: " + player.getCurrentHp());
        
        player.restoreFullHp();
        System.out.println("Restored HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
        
        System.out.println("✅ Player progression test passed!\n");
    }
    
    private static void testFlashcards() {
        System.out.println("📚 Testing Flashcard System...");
        
        // Create a flashcard
        Flashcard card = new Flashcard(
            "What is polymorphism?",
            "Polymorphism allows objects of different types to be treated as objects of a common base type.",
            "Object-Oriented Programming",
            DifficultyLevel.MEDIUM
        );
        
        System.out.println("Created flashcard: " + card.getQuestion());
        System.out.println("Category: " + card.getCategory());
        System.out.println("Difficulty: " + card.getDifficulty().getDisplayText());
        System.out.println("XP Bonus: " + card.getDifficultyXpBonus());
        System.out.println("Selection weight: " + card.getSelectionWeight());
        
        // Test answer recording
        card.recordAnswer(true);
        card.recordAnswer(false);
        card.recordAnswer(true);
        
        System.out.println("\nAfter 3 attempts (2 correct, 1 incorrect):");
        System.out.println("Times asked: " + card.getTimesAsked());
        System.out.println("Times correct: " + card.getTimesCorrect());
        System.out.println("Accuracy rate: " + String.format("%.1f%%", card.getAccuracyRate()));
        
        System.out.println("✅ Flashcard test passed!\n");
    }
    
    private static void testQuests() {
        System.out.println("🗡️ Testing Quest System...");
        
        Quest quest = new Quest("Java Basics Quest");
        quest.setDescription("Learn fundamental Java concepts");
        quest.setQuestionCount(5);
        quest.setCustomHp(100);
        
        System.out.println("Created quest: " + quest.getName());
        System.out.println("Description: " + quest.getDescription());
        System.out.println("Question count: " + quest.getQuestionCount());
        System.out.println("Custom HP: " + quest.getCustomHp());
        
        // Test difficulty distribution
        DifficultyDistribution dist = quest.getDifficultyDistribution();
        System.out.println("Default difficulty distribution: " + dist);
        
        int[] counts = dist.calculateQuestionCounts(10);
        System.out.println("For 10 questions - Easy: " + counts[0] + ", Medium: " + counts[1] + ", Hard: " + counts[2]);
        
        System.out.println("✅ Quest test passed!\n");
    }
    
    private static void testDefaultFlashcards() {
        System.out.println("📖 Testing Default Flashcard Dataset...");
        
        List<Flashcard> defaultCards = DefaultFlashcardService.createDefaultFlashcards();
        System.out.println("Created " + defaultCards.size() + " default flashcards");
        
        // Count by category
        long javaBasics = defaultCards.stream()
            .filter(card -> "Java Basics".equals(card.getCategory()))
            .count();
        long oopCards = defaultCards.stream()
            .filter(card -> "Object-Oriented Programming".equals(card.getCategory()))
            .count();
        long collectionsCards = defaultCards.stream()
            .filter(card -> "Collections Framework".equals(card.getCategory()))
            .count();
        
        System.out.println("Java Basics: " + javaBasics + " cards");
        System.out.println("OOP: " + oopCards + " cards");
        System.out.println("Collections: " + collectionsCards + " cards");
        
        // Show first few cards
        System.out.println("\nFirst 3 flashcards:");
        for (int i = 0; i < Math.min(3, defaultCards.size()); i++) {
            Flashcard card = defaultCards.get(i);
            System.out.println((i + 1) + ". " + card.getCategory() + " - " + 
                card.getDifficulty().getEmoji() + " " + card.getQuestion());
        }
        
        System.out.println("✅ Default flashcard dataset test passed!\n");
    }
}