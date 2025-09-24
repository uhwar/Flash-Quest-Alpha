public class XPProgressionTest {
    public static void main(String[] args) {
        System.out.println("Testing XP Progression Curve...");
        System.out.println("Expected (from rules): 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700, 3250...");
        
        System.out.print("Current formula output: ");
        for (int level = 2; level <= 11; level++) {
            int xp = calculateXpRequiredForLevel(level);
            System.out.print(xp);
            if (level < 11) System.out.print(", ");
        }
        System.out.println();
        
        // Test the corrected formula
        System.out.print("Corrected formula output: ");
        for (int level = 2; level <= 11; level++) {
            int xp = calculateCorrectedXpRequiredForLevel(level);
            System.out.print(xp);
            if (level < 11) System.out.print(", ");
        }
        System.out.println();
    }
    
    // Current formula (what we have)
    private static int calculateXpRequiredForLevel(int level) {
        if (level <= 1) return 0;
        int n = level - 1;
        return 100 + (n * 150) + (n * n * 25);
    }
    
    // Corrected formula to match the specification
    private static int calculateCorrectedXpRequiredForLevel(int level) {
        if (level <= 1) return 0;
        int n = level - 1;
        return (n * 150) + (n * n * 50) + 100;
    }
}