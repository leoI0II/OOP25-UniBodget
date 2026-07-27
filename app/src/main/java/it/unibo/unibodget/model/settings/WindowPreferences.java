package it.unibo.unibodget.model.settings;

/**
 * Represents the persisted window configuration of the application.
 * Stores width, height and maximized state so the UI can be restored
 * exactly as the user left it in the previous session.
 */
public final class WindowPreferences {

    private static final int WIDTH_1280 = 1280;
    private static final int HEIGHT_720 = 720;
    private double width;
    private double height;
    private boolean maximized;

    /**
     * Creates a new set of window preferences with the given size and state.
     *
     * @param width      the window width in pixels
     * @param height     the window height in pixels
     * @param maximized  true if the window was maximized, false otherwise
     */
    public WindowPreferences(final double width, final double height, final boolean maximized) {
        this.width = width;
        this.height = height;
        this.maximized = maximized;
    }

    /**
     * Creates a default window preference set.
     * This constructor is required by JSON deserializers such as Jackson.
     * Defaults to a 1280×720 window, not maximized.
     */
    public WindowPreferences() {
        this(WIDTH_1280, HEIGHT_720, false);
    }

    /**
     * Returns the stored window width.
     *
     * @return the width in pixels
     */
    public double getWidth() { 
        return this.width; 
    }

    /**
     * Updates the stored window width.
     *
     * @param width the new width in pixels
     */
    public void setWidth(final double width) { 
        this.width = width; 
    }

    /**
     * Returns the stored window height.
     *
     * @return the height in pixels
     */
    public double getHeight() { 
        return this.height; 
    }

    /**
     * Updates the stored window height.
     *
     * @param height the new height in pixels
     */
    public void setHeight(final double height) { 
        this.height = height; 
    }

    /**
     * Returns whether the window was maximized.
     *
     * @return true if maximized, false otherwise
     */
    public boolean isMaximized() { 
        return this.maximized; 
    }

    /**
     * Updates the maximized state of the window.
     *
     * @param maximized true if the window is maximized, false otherwise
     */
    public void setMaximized(final boolean maximized) { 
        this.maximized = maximized; 
    }

}
