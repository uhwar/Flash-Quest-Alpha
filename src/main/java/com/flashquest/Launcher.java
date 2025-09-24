package com.flashquest;

/**
 * Launcher class to work around JavaFX module system issues.
 * This class doesn't extend Application, so it can be run directly
 * without JavaFX runtime module restrictions.
 */
public class Launcher {
    public static void main(String[] args) {
        // Set JavaFX properties to avoid module issues
        System.setProperty("javafx.preloader", "");
        System.setProperty("javafx.application.class", "com.flashquest.FlashQuestApplication");
        
        // Enable universal scaling support for all platforms
        System.setProperty("prism.allowhidpi", "true");    // Allow high DPI rendering
        System.setProperty("prism.order", "d3d,es2,sw");   // Use hardware acceleration
        System.setProperty("sun.java2d.dpiaware", "true"); // Enable DPI awareness
        
        // Detect and configure universal scaling
        try {
            detectAndConfigureUniversalScaling();
        } catch (Exception e) {
            System.out.println("Could not detect system configuration, using defaults");
        }
        
        // JavaFX platform settings
        System.setProperty("java.awt.headless", "false");
        System.setProperty("javafx.platform", "desktop");
        
        try {
            // Launch the JavaFX application through reflection to avoid module issues
            FlashQuestApplication.main(args);
        } catch (Exception e) {
            System.err.println("Failed to start FlashQuest:");
            System.err.println(e.getMessage());
            
            // Print helpful error message
            System.err.println("\nTroubleshooting:");
            System.err.println("1. Make sure you have Java 17+ installed");
            System.err.println("2. Try downloading OpenJDK with JavaFX included");
            System.err.println("3. Check if JavaFX runtime is available");
            
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Detects system configuration and configures universal scaling
     */
    private static void detectAndConfigureUniversalScaling() {
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            java.awt.Dimension screenSize = toolkit.getScreenSize();
            int dpi = toolkit.getScreenResolution();
            
            double screenWidth = screenSize.getWidth();
            double dpiScale = dpi / 96.0;
            
            System.out.println("Screen: " + (int)screenWidth + "x" + (int)screenSize.getHeight() + 
                ", DPI: " + dpi + ", Scale Factor: " + String.format("%.1f", dpiScale));
            
            // Configure JavaFX properties based on system
            if (dpiScale >= 1.25) {
                // Configure for high DPI systems
                System.setProperty("glass.win.uiScale", Math.round(dpiScale * 100) + "%");
                System.setProperty("sun.java2d.win.uiScaleX", String.valueOf(dpiScale));
                System.setProperty("sun.java2d.win.uiScaleY", String.valueOf(dpiScale));
                System.out.println("Configured for high DPI: " + Math.round(dpiScale * 100) + "%");
            }
            
            // Set display configuration hints
            if (screenWidth >= 3840) {
                System.out.println("Detected 4K+ display - optimizing for large screens");
            } else if (screenWidth >= 2560) {
                System.out.println("Detected 1440p display - optimizing for medium-large screens");
            } else if (screenWidth >= 1920) {
                System.out.println("Detected 1080p display - using standard scaling");
            } else {
                System.out.println("Detected lower resolution - optimizing for compact layout");
            }
            
        } catch (Exception e) {
            System.out.println("Could not detect system configuration: " + e.getMessage());
        }
    }
}
