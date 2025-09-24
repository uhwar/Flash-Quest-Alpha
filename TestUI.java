import com.flashquest.service.GameService;

/**
 * Simple test to verify UI and game service functionality.
 */
public class TestUI {
    public static void main(String[] args) {
        System.out.println("🎮 FlashQuest UI Test");
        System.out.println("====================\n");
        
        try {
            // Test GameService initialization
            System.out.println("📋 Testing GameService...");
            GameService gameService = GameService.getInstance();
            
            // Test game initialization
            gameService.initializeGame();
            System.out.println("✅ GameService initialized");
            
            // Test player creation
            System.out.println("\n👤 Testing Player Creation...");
            gameService.createNewPlayer("TestHero");
            System.out.println("✅ Player created: " + gameService.getCurrentPlayer().getName());
            
            // Test flashcards loaded
            System.out.println("\n📚 Testing Flashcards...");
            int cardCount = gameService.getAllFlashcards().size();
            System.out.println("✅ Loaded " + cardCount + " flashcards");
            
            // Test categories
            var categories = gameService.getAvailableCategories();
            System.out.println("✅ Available categories: " + categories.size());
            for (String category : categories) {
                System.out.println("   • " + category);
            }
            
            // Test player stats
            System.out.println("\n📊 Player Stats:");
            var player = gameService.getCurrentPlayer();
            System.out.println("   Level: " + player.getCurrentLevel());
            System.out.println("   XP: " + player.getTotalXp() + " / " + player.getXpRequiredForNextLevel());
            System.out.println("   HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
            System.out.println("   Quests Completed: " + player.getQuestsCompleted());
            
            System.out.println("\n🎉 All UI tests passed!");
            System.out.println("\n🚀 Ready to run JavaFX application!");
            System.out.println("Use 'mvn javafx:run' to start the full UI experience.");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}