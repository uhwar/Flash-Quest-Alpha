public class FinalXPFormula {
    public static void main(String[] args) {
        System.out.println("Final XP Formula Derivation");
        System.out.println("===========================");
        
        // Expected values from the rules (XP required to REACH that level)
        int[] expected = {0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700, 3250};
        
        // The correct interpretation:
        // Level 1 → Level 2: 100 XP
        // Level 2 → Level 3: 150 XP (so 100 + 150 = 250 total)
        // Level 3 → Level 4: 200 XP (so 250 + 200 = 450 total)
        // etc.
        
        System.out.println("Level progression costs:");
        System.out.println("Start → Level 1: 0 XP");
        System.out.println("Level 1 → Level 2: 100 XP (total: 100)");
        for (int i = 2; i < expected.length - 1; i++) {
            int cost = expected[i+1] - expected[i];
            System.out.println("Level " + i + " → Level " + (i+1) + ": " + cost + " XP (total: " + expected[i+1] + ")");
        }
        
        // Test our formula implementation
        System.out.println("\nTesting our formula against expected values:");
        for (int level = 1; level <= 10; level++) {
            int calculated = getXpRequiredForLevel(level);
            int expectedXp = level < expected.length ? expected[level] : -1;
            String status = (calculated == expectedXp) ? "✓" : "✗";
            
            System.out.println("Level " + level + ": " + calculated + " XP " + 
                             (expectedXp >= 0 ? "(expected: " + expectedXp + ") " + status : ""));
        }
    }
    
    /**
     * The correct formula based on the FlashQuest rules.
     * Each level up costs: 100 + (level-2) * 50
     * So: Level 2 costs 100, Level 3 costs 150, Level 4 costs 200, etc.
     */
    public static int getXpRequiredForLevel(int level) {
        if (level <= 1) return 0;
        
        int totalXp = 0;
        for (int i = 2; i <= level; i++) {
            int levelCost = 100 + (i - 2) * 50;
            totalXp += levelCost;
        }
        return totalXp;
    }
    
    /**
     * Optimized closed-form version of the same formula
     */
    public static int getXpRequiredForLevelOptimized(int level) {
        if (level <= 1) return 0;
        int n = level - 1;
        return 75 * n + 25 * n * n;
    }
}