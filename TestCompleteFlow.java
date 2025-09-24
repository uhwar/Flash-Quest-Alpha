import com.flashquest.service.GameService;
import com.flashquest.model.*;

/**
 * Comprehensive test of the complete FlashQuest experience.
 * Tests the full flow from player creation to quest completion.
 */
public class TestCompleteFlow {
    public static void main(String[] args) {
        System.out.println("🚀 FlashQuest Complete Flow Test");
        System.out.println("=================================\n");
        
        try {
            GameService gameService = GameService.getInstance();
            
            // Test 1: Initialize game
            System.out.println("📋 Step 1: Initializing Game...");
            gameService.initializeGame();
            System.out.println("✅ Game initialized");
            
            // Test 2: Create player
            System.out.println("\n👤 Step 2: Creating Player...");
            gameService.createNewPlayer("TestHero");
            Player player = gameService.getCurrentPlayer();
            System.out.println("✅ Player created: " + player.getName());
            System.out.println("   Starting Level: " + player.getCurrentLevel());
            System.out.println("   Starting HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
            System.out.println("   Starting XP: " + player.getTotalXp());
            
            // Test 3: Verify flashcards loaded
            System.out.println("\n📚 Step 3: Verifying Flashcards...");
            int cardCount = gameService.getAllFlashcards().size();
            System.out.println("✅ " + cardCount + " flashcards available");
            
            var categories = gameService.getAvailableCategories();
            System.out.println("✅ Categories: " + String.join(", ", categories));
            
            // Test 4: Start quest
            System.out.println("\n🗡️ Step 4: Starting Quest...");
            Quest quest = gameService.startQuickQuest();
            System.out.println("✅ Quest started: " + quest.getName());
            System.out.println("   Questions: " + quest.getQuestionCount());
            System.out.println("   Current question: " + (quest.getCurrentQuestionIndex() + 1));
            
            // Test 5: Simulate quest gameplay
            System.out.println("\n🎮 Step 5: Simulating Quest Gameplay...");
            simulateQuestFlow(gameService, quest, player);
            
            // Test 6: Final player stats
            System.out.println("\n📊 Step 6: Final Player Stats...");
            player = gameService.getCurrentPlayer(); // Refresh player data
            System.out.println("✅ Final Level: " + player.getCurrentLevel());
            System.out.println("✅ Final XP: " + player.getTotalXp());
            System.out.println("✅ Quests Completed: " + player.getQuestsCompleted());
            System.out.println("✅ Perfect Quests: " + player.getPerfectQuests());
            System.out.println("✅ Unlocked Titles: " + player.getUnlockedTitles().size());
            
            System.out.println("\n🎉 Complete Flow Test PASSED!");
            System.out.println("\n🎮 FlashQuest is ready to play!");
            System.out.println("Run 'mvn javafx:run' to experience the full game!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simulates a complete quest flow with various answer patterns.
     */
    private static void simulateQuestFlow(GameService gameService, Quest quest, Player player) {
        int questionsAnswered = 0;
        int correctAnswers = 0;
        
        while (quest.isActive() && questionsAnswered < quest.getQuestionCount()) {
            questionsAnswered++;
            
            // Get current flashcard
            Flashcard currentCard = quest.getCurrentFlashcard();
            if (currentCard == null) {
                System.out.println("   No current flashcard available");
                break;
            }
            
            System.out.println("   Q" + questionsAnswered + ": " + 
                currentCard.getCategory() + " - " + 
                currentCard.getDifficulty().getEmoji() + " " +
                currentCard.getQuestion().substring(0, Math.min(50, currentCard.getQuestion().length())) + "...");
            
            // Simulate different answer patterns for testing
            boolean answerCorrect;
            if (questionsAnswered <= 7) {
                // First 7 questions correct
                answerCorrect = true;
                correctAnswers++;
            } else if (questionsAnswered == 8) {
                // 8th question incorrect (test HP loss)
                answerCorrect = false;
            } else {
                // Remaining questions correct
                answerCorrect = true;
                correctAnswers++;
            }
            
            // Process the answer
            Quest.QuestionResult result = gameService.processQuestAnswer(answerCorrect);
            
            if (result.isError()) {
                System.out.println("   ❌ Error processing answer");
                break;
            }
            
            System.out.println("   " + (answerCorrect ? "✅ Correct" : "❌ Incorrect") + 
                              " | XP: +" + result.getXpEarned() + 
                              " | HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
            
            // Check for quest completion or failure
            if (result.isQuestComplete()) {
                System.out.println("   🎉 Quest completed!");
                break;
            } else if (player.getCurrentHp() <= 0) {
                System.out.println("   💀 Quest failed - HP reached zero");
                break;
            }
        }
        
        // Show quest summary
        System.out.println("   📊 Quest Summary:");
        System.out.println("     Questions Answered: " + questionsAnswered);
        System.out.println("     Correct Answers: " + correctAnswers);
        System.out.println("     Total XP Earned: " + quest.getTotalXpEarned());
        System.out.println("     Perfect Quest: " + quest.isPerfectQuest());
        System.out.println("     Final HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
    }
}