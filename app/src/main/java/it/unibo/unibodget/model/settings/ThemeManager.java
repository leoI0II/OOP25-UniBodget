package it.unibo.unibodget.model.settings;

import java.util.Objects;

/**
 * Manages the currently active {@link Theme} used by the application UI.
 */
public final class ThemeManager {

    private static Theme currentTheme = Theme.DEFAULT;

    private ThemeManager() { }

    /**
     * Returns the currently active theme.
     *
     * @return the current theme
     */
    public static Theme getTheme() {
        return currentTheme;
    }

    /**
     * Updates the currently active theme.
     *
     * @param theme the new theme
     */
    public static void setTheme(final Theme theme) {
        currentTheme = Objects.requireNonNull(theme);
    }
}