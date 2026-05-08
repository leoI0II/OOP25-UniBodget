package it.unibo.unibodget.model.settings;

import java.util.Objects;

/**
 * Manages the currently active {@link Theme} used by the application UI.
 * 
 * {@code ThemeManager} acts as a simple global holder for the selected theme.
 * It provides static methods to retrieve or update the active theme, ensuring
 * that all UI components can access consistent visual settings.
 * 
 * The manager initializes with {@link Theme#DEFAULT} unless explicitly changed
 * by the application (e.g., during startup or through user preferences).
 */
public final class ThemeManager {

    /**
     * The theme currently in use by the application.
     * Initialized to {@link Theme#DEFAULT}.
     */
    private static Theme currentTheme = Theme.DEFAULT;

    /**
     * Private constructor to prevent instantiation.
     */
    private ThemeManager() { }

    /**
     * Returns the theme currently active in the application.
     *
     * @return the active {@link Theme}
     */
    public static Theme getTheme() {
        return currentTheme;
    }

    /**
     * Sets the theme to be used by the application.
     * The provided theme must not be {@code null}.
     *
     * @param theme the new theme to activate
     * @throws NullPointerException if {@code theme} is null
     */
    public static void setTheme(Theme theme) {
        currentTheme = Objects.requireNonNull(theme);
    }
}
