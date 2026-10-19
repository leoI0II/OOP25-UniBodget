package it.unibo.unibodget.model.settings;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

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

    private static final List<Consumer<Theme>> LISTENERS =
            new CopyOnWriteArrayList<>();

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
     * Updates the currently active theme and notifies all registered scenes and listeners.
     *
     * @param theme the new theme (must not be null)
     * @throws NullPointerException if {@code theme} is null
     */
    public static void setTheme(final Theme theme) {
        currentTheme = Objects.requireNonNull(theme);

        for (final Scene scene : SCENES.toArray(new Scene[0])) {
            applyThemeToScene(scene);
        }

        for (final Consumer<Theme> listener : LISTENERS) {
            listener.accept(currentTheme);
        }
    }

    /**
     * Registers a listener to be notified when the theme changes.
     *
     * @param listener the consumer to receive theme update notifications
     */
    public static void addListener(final Consumer<Theme> listener) {
        LISTENERS.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Unregisters a theme change listener.
     *
     * @param listener the listener to remove
     */
    public static void removeListener(final Consumer<Theme> listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Stylesheet resource path for the unified application theme.
     */
    private static final String THEME_CSS_PATH = "/css/theme.css";

    /**
     * Applies the active theme to the given JavaFX {@link Scene}.
     *
     * <p>The method updates:</p>
     * <ul>
     *     <li>attached theme stylesheets</li>
     *     <li>theme mode style class (theme-dark / theme-light)</li>
     *     <li>font family, size and weight</li>
     *     <li>background gradient</li>
     * </ul>
     *
     * <p>Styles are applied directly to the root node using inline CSS and stylesheet rules.</p>
     *
     * @param scene the scene to style
     */
    public static void applyThemeToScene(final Scene scene) {
        Objects.requireNonNull(scene, "scene");
        SCENES.add(scene);

        final var stylesheetUrl = ThemeManager.class.getResource(THEME_CSS_PATH);
        if (stylesheetUrl != null) {
            final String ext = stylesheetUrl.toExternalForm();
            if (!scene.getStylesheets().contains(ext)) {
                scene.getStylesheets().add(ext);
            }
        }

        final Parent root = scene.getRoot();
        if (root != null) {
            root.getStyleClass().removeAll("theme-dark", "theme-light");
            root.getStyleClass().add(currentTheme.isDarkMode() ? "theme-dark" : "theme-light");

            applyBackground(root);
            root.applyCss();
            applyToTree(root);
        }
    }

    /**
     * Applies the active theme to the given JavaFX {@link Dialog}.
     *
     * <p>Configures the dialog pane stylesheets and theme mode class, and registers
     * a hook to style the underlying scene when the dialog is shown.</p>
     *
     * @param dialog the dialog to style (must not be null)
     */
    public static void applyThemeToDialog(final Dialog<?> dialog) {
        Objects.requireNonNull(dialog, "dialog");
        final DialogPane dialogPane = dialog.getDialogPane();
        if (dialogPane != null) {
            final var stylesheetUrl = ThemeManager.class.getResource(THEME_CSS_PATH);
            if (stylesheetUrl != null) {
                final String ext = stylesheetUrl.toExternalForm();
                if (!dialogPane.getStylesheets().contains(ext)) {
                    dialogPane.getStylesheets().add(ext);
                }
            }
            dialogPane.getStyleClass().removeAll("theme-dark", "theme-light");
            dialogPane.getStyleClass().add(currentTheme.isDarkMode() ? "theme-dark" : "theme-light");
            applyBackground(dialogPane);
            applyToTree(dialogPane);

            if (dialogPane.getScene() != null) {
                applyThemeToScene(dialogPane.getScene());
            }
        }

        final var existingOnShown = dialog.getOnShown();
        dialog.setOnShown(event -> {
            if (dialog.getDialogPane() != null && dialog.getDialogPane().getScene() != null) {
                applyThemeToScene(dialog.getDialogPane().getScene());
            }
            if (existingOnShown != null) {
                existingOnShown.handle(event);
            }
        });
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
        final String existingStyle = node.getStyle()
                .replaceAll("(?i)-fx-background-color\\s*:[^;]*(?:;|$)", "")
                .replaceAll("(?i)-fx-accent\\s*:[^;]*(?:;|$)", "");

        node.setStyle(existingStyle + "; " + getPrimaryBackgroundStyle());
    }

    /**
     * Checks if the currently active theme is dark mode.
     *
     * @return {@code true} if dark mode, {@code false} if light mode
     */
    public static boolean isDarkMode() {
        return getTheme().isDarkMode();
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
     * Gets the CSS style string for the primary background of the current theme.
     * 
     * @return the CSS style string for the solid background
     */
    public static String getPrimaryBackgroundStyle() {
        if (getTheme().isDarkMode()) {
            return "-fx-background-color: #0b0e17;";
        }
        return "-fx-background-color: #f4f6fa;";
    }

    
}
