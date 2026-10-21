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

    private static final String THEME_CSS_PATH = "/css/theme.css";
    private static final String THEME_DARK = "theme-dark";
    private static final String THEME_LIGHT = "theme-light";
    private static final String PROP_THEME_APPLIED_BOLD = "themeAppliedBold";
    private static final String CSS_QUOTE_SEMICOLON = "';";
    private static final String CSS_FONT_FAMILY_PREFIX = "-fx-font-family: '";
    private static final int MAX_PARENT_SEARCH_DEPTH = 3;

    private static final Set<String> BOLD_STYLE_CLASSES = Set.of(
            "wallet-tile-name",
            "wallet-name",
            "wallet-header",
            "card-header",
            "balance-big",
            "stat-value",
            "asset-name",
            "stat-change",
            "button-primary",
            "primary-button",
            "pill-toggle",
            "toggle-button",
            "column-header",
            "tab-label",
            "bold",
            "header-title"
    );

    private static final Set<Scene> SCENES =
            Collections.newSetFromMap(new WeakHashMap<>());

    private static final List<Consumer<Theme>> LISTENERS =
            new CopyOnWriteArrayList<>();

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
            root.getStyleClass().removeAll(THEME_DARK, THEME_LIGHT);
            root.getStyleClass().add(currentTheme.isDarkMode() ? THEME_DARK : THEME_LIGHT);

            final String existingStyle = root.getStyle().replaceAll(
                    "(?i)-fx-font-family\\s*:[^;]*(?:;|$)",
                    ""
            );
            root.setStyle(existingStyle + "; " + CSS_FONT_FAMILY_PREFIX
                    + currentTheme.getFontFamily() + CSS_QUOTE_SEMICOLON);

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
            dialogPane.getStyleClass().removeAll(THEME_DARK, THEME_LIGHT);
            dialogPane.getStyleClass().add(currentTheme.isDarkMode() ? THEME_DARK : THEME_LIGHT);
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
     * Checks if the given JavaFX node is designated as bold by default.
     *
     * @param node the node to check
     * @return true if the node is bold by default, false otherwise
     */
    private static boolean isBoldByDefault(final Node node) {
        if (Boolean.TRUE.equals(node.getProperties().get("boldByDefault"))) {
            return true;
        }
        for (final String sc : node.getStyleClass()) {
            if (BOLD_STYLE_CLASSES.contains(sc)) {
                return true;
            }
        }
        final String style = node.getStyle();
        if (style != null && style.matches("(?i).*?-fx-font-weight\\s*:\\s*bold.*")) {
            return !Boolean.TRUE.equals(node.getProperties().get(PROP_THEME_APPLIED_BOLD));
        }
        return false;
    }

    /**
     * Checks if the given node or any of its direct ancestors is bold by default.
     *
     * @param node the node to inspect
     * @return true if the node or an ancestor is bold by default
     */
    private static boolean isNodeOrParentBoldByDefault(final Node node) {
        if (isBoldByDefault(node)) {
            return true;
        }
        Node current = node.getParent();
        for (int i = 0; i < MAX_PARENT_SEARCH_DEPTH && current != null; i++) {
            if (isBoldByDefault(current)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Applies the active theme font to the given JavaFX {@link Node}.
     *
     * <p>The method updates:</p>
     * <ul>
     *     <li>font family</li>
     *     <li>font weight, keeping nodes that are bold by default untouched</li>
     * </ul>
     *
     * <p>Font styles are applied directly to the node using inline CSS,
     * preserving any existing styles.</p>
     *
     * @param node the JavaFX node to style
     */
    public static void applyFont(final Node node) {
        Objects.requireNonNull(node, "node");

        final boolean boldDefault = isNodeOrParentBoldByDefault(node);
        final boolean effectiveBold = boldDefault || currentTheme.isBoldText();

        final String existingStyle = node.getStyle().replaceAll(
                "(?i)-fx-font-(?:family|weight)"
                        + "\\s*:[^;]*(?:;|$)",
                ""
        );

        final String familyStyle = CSS_FONT_FAMILY_PREFIX + currentTheme.getFontFamily() + CSS_QUOTE_SEMICOLON;

        if (boldDefault) {
            node.setStyle(existingStyle + ";" + familyStyle + "-fx-font-weight: bold;");
            node.getProperties().remove(PROP_THEME_APPLIED_BOLD);
        } else if (effectiveBold) {
            node.setStyle(existingStyle + ";" + familyStyle + "-fx-font-weight: bold;");
            node.getProperties().put(PROP_THEME_APPLIED_BOLD, Boolean.TRUE);
        } else {
            node.setStyle(existingStyle + ";" + familyStyle + "-fx-font-weight: normal;");
            node.getProperties().remove(PROP_THEME_APPLIED_BOLD);
        }
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
     * @return the CSS style string for font family and weight
     */
    public static String getFontStyle() {
        final Theme t = getTheme();

        return "-fx-font-family: '" + t.getFontFamily() + "';"
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
