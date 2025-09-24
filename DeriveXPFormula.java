public class DeriveXPFormula {
    public static void main(String[] args) {
        System.out.println("Deriving the correct XP formula...");
        
        // Expected values from the rules
        int[] expected = {0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700, 3250};
        
        System.out.println("Expected progression:");
        for (int i = 1; i < expected.length; i++) {
            System.out.println("Level " + i + ": " + expected[i-1] + " XP");
        }
        
        // Analyze the differences
        System.out.println("\nDifferences between levels:");
        for (int i = 2; i < expected.length; i++) {
            int diff = expected[i] - expected[i-1];
            System.out.println("Level " + (i-1) + " to " + i + ": +" + diff + " XP");
        }
        
        // The pattern: 100, 150, 200, 250, 300, 350, 400, 450, 500, 550
        // This suggests: base 100 + (level-2) * 50
        
        System.out.println("\nTesting derived formula:");
        System.out.println("For level N: sum from i=2 to N of (50 + (i-2)*50)");
        
        for (int level = 1; level <= 10; level++) {
            int calculatedXp = calculateDerivedXp(level);
            int expectedXp = expected[level];
            boolean matches = (calculatedXp == expectedXp);
            
            System.out.println("Level " + level + ": calculated=" + calculatedXp + 
                             ", expected=" + expectedXp + 
                             (matches ? " ✓" : " ✗"));
        }
    }
    
    private static int calculateDerivedXp(int level) {
        if (level <= 1) return 0;
        
        // The XP required is the sum of all "level up costs" from 2 to level
        // Level 2 costs 100, level 3 costs 150, level 4 costs 200, etc.
        // Cost for level N = 100 + (N-2) * 50 = 50 + N * 50
        
        int totalXp = 0;
        for (int i = 2; i <= level; i++) {
            int levelCost = 50 + i * 50; // This gives us 100, 150, 200, 250...
            totalXp += levelCost;
        }
        return totalXp;
    }
}