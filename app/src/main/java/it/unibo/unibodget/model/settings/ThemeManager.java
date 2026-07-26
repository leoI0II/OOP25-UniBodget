package it.unibo.unibodget.model.settings;

import java.util.Objects;

import javafx.scene.Scene;

/**
 * Manages the currently active {@link Theme} used by the application UI.
 *
 * <p>{@code ThemeManager} acts as a simple global holder for the selected theme.
 * It provides static methods to retrieve or update the active theme, ensuring
 * consistent visual settings across all UI components.</p>
 *
 * <p>The manager starts with {@link Theme#DEFAULT} unless changed explicitly
 * by the application (e.g., through user preferences).</p>
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
    private ThemeManager() { 

    }

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
     * @param theme the new theme (must not be null)
     * @throws NullPointerException if {@code theme} is null
     */
    public static void setTheme(final Theme theme) {
        currentTheme = Objects.requireNonNull(theme);
    }

    /**
     * Applies the active theme to the given JavaFX {@link Scene}.
     *
     * <p>The method updates:</p>
     * <ul>
     *     <li>font family</li>
     *     <li>font size</li>
     *     <li>font weight (bold/normal)</li>
     *     <li>background color</li>
     * </ul>
     *
     * <p>Styles are applied directly to the root node using inline CSS.</p>
     *
     * @param scene the scene to style
     */
    public static void applyThemeToScene(final Scene scene) {
        // Retrieve active theme
        final Theme t = getTheme();
        final String style = ""
            + "-fx-font-family: '" + t.getFontFamily() + "';"
            + "-fx-font-size: " + t.getFontSize() + "px;"
            + (t.isBoldText() ? "-fx-font-weight: bold;" : "-fx-font-weight: normal;")
            + "-fx-background-color: " + t.getPrimaryColor().toHex() + ";";

        // Apply style to the scene root
        scene.getRoot().setStyle(style);
    }
}
