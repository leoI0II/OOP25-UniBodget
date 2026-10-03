package it.unibo.unibodget.model.settings;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import it.unibo.unibodget.model.utils.ARGBColor;
import javafx.scene.Node;
import javafx.scene.Parent;
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

    private static final Set<Scene> SCENES =
            Collections.newSetFromMap(new WeakHashMap<>());

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

        for (final Scene scene : SCENES.toArray(new Scene[0])) {
            applyThemeToScene(scene);
        }
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
        Objects.requireNonNull(scene, "scene");
        SCENES.add(scene);

        applyBackground(scene.getRoot());

        // Creates control skins, including ScrollPane viewports.
        scene.getRoot().applyCss();

        applyToTree(scene.getRoot());

        System.out.println("[THEME] Applied: "
                + currentTheme.getPrimaryColor().toHexString()
                + ", font: " + currentTheme.getFontFamily()
                + ", size: " + currentTheme.getFontSize());
    }

    /**
     * Recursively applies the active theme to the given JavaFX {@link Node} and its children.
     * 
     * @param node the JavaFX node to style
     */
    private static void applyToTree(final Node node) {
        applyFont(node);

        if (node.getStyleClass().contains("theme-background")) {
            applyBackground(node);
        }

        if (node.getStyleClass().contains("theme-scroll")) {
            applyBackground(node);

            for (final Node viewport : node.lookupAll(".viewport")) {
                applyBackground(viewport);
            }
        }

        if (node instanceof Parent parent) {
            for (final Node child : parent.getChildrenUnmodifiable()) {
                applyToTree(child);
            }
        }
    }

    /**
     * Applies the active theme font to the given JavaFX {@link Node}.
     *
     * <p>The method updates:</p>
     * <ul>
     *     <li>font family</li>
     *     <li>font size</li>
     *     <li>font weight (bold/normal)</li>
     * </ul>
     *
     * <p>Font styles are applied directly to the node using inline CSS,
     * preserving any existing styles.</p>
     *
     * @param node the JavaFX node to style
     */
    public static void applyFont(final Node node) {
        Objects.requireNonNull(node, "node");

        final String existingStyle = node.getStyle().replaceAll(
                "(?i)-fx-font-(?:family|size|weight)"
                        + "\\s*:[^;]*(?:;|$)",
                ""
        );

        node.setStyle(existingStyle + ";" + getFontStyle());
    }

    private static void applyBackground(final Node node) {
        final String existingStyle = node.getStyle().replaceAll(
                "(?i)-fx-background-color\\s*:[^;]*(?:;|$)",
                ""
        );

        node.setStyle(existingStyle + ";" + getPrimaryBackgroundStyle());
    }

    /**
     * Get the CSS style string for the font settings of the current theme.
     * 
     * @return the CSS style string for font family, size, and weight
     */
    public static String getFontStyle() {
        final Theme t = getTheme();

        return "-fx-font-family: '" + t.getFontFamily() + "';"
                + "-fx-font-size: " + t.getFontSize() + "px;"
                + "-fx-font-weight: "
                + (t.isBoldText() ? "bold;" : "normal;");
    }

    /**
     * Gets the CSS style string for the primary background color of the current theme.
     * 
     * @return the CSS style string for the primary background color
     */
    public static String getPrimaryBackgroundStyle() {
        final ARGBColor color = getTheme().getPrimaryColor();

        return String.format(
                Locale.ROOT,
                "-fx-background-color: rgba(%d,%d,%d,%.4f);",
                color.red(),
                color.green(),
                color.blue(),
                color.alpha() / 255.0
        );
    }
    
}
