package it.unibo.unibodget;

/**
 * Entry point for launching the packaged application.
 */
public final class Launcher {

    private Launcher() {
        // Prevent instantiation.
    }

    /**
     * Starts the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        App.main(args);
    }
}
