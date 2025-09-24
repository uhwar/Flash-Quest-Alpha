package com.flashquest.ui;

/**
 * Interface for screen controllers that need access to the main AppController
 * for navigation and app-level operations.
 */
public interface ScreenController {
    
    /**
     * Sets the main application controller reference.
     * Called automatically when a screen is loaded.
     */
    void setAppController(AppController appController);
    
    /**
     * Called when the screen is being shown.
     * Override to perform initialization or refresh operations.
     */
    default void onScreenShown() {
        // Default implementation does nothing
    }
    
    /**
     * Called when the screen is being hidden.
     * Override to perform cleanup operations.
     */
    default void onScreenHidden() {
        // Default implementation does nothing
    }
}