package it.unibo.unibodget.model.settings;

import it.unibo.unibodget.model.utils.ARGBColor;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the active theme and updates registered scenes.
 *
 * <p>Font sizes remain defined by individual views, controls, FXML files or stylesheets.
 */
public final class ThemeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeManager.class);

    private static Theme currentTheme = Theme.DEFAULT;

    private static final Set<Scene> SCENES = Collections.newSetFromMap(new WeakHashMap<>());

    private static final double MAX_COLOR_COMPONENT = 255.0;

    /** Prevents instantiation. */
    private ThemeManager() {
        // utility class
    }

    /**
     * Returns the active theme.
     *
     * @return the active theme
     */
    public static Theme getTheme() {
        return currentTheme;
    }

    /**
     * Updates the theme and reapplies it to registered scenes.
     *
     * @param theme the new theme
     * @throws NullPointerException if the theme is null
     */
    public static void setTheme(final Theme theme) {
        currentTheme = Objects.requireNonNull(theme, "theme");

        for (final Scene scene : SCENES.toArray(new Scene[0])) {
            applyThemeToScene(scene);
        }
    }

    /**
     * Applies typography, backgrounds and label contrast to a scene.
     *
     * <p>Existing font sizes are preserved.
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

        LOGGER.debug(
                "[THEME] Applied: {}, font: {}",
                currentTheme.getPrimaryColor().toHexString(),
                currentTheme.getFontFamily());
    }

    /**
     * Recursively applies the theme to a node and its children.
     *
     * @param node the node to style
     */
    private static void applyToTree(final Node node) {
        applyFont(node);
        applyTextColor(node);

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
     * Applies theme font family and weight while preserving font size.
     *
     * @param node the node to style
     */
    public static void applyFont(final Node node) {
        Objects.requireNonNull(node, "node");

        final String existingStyle =
                node.getStyle().replaceAll("(?i)-fx-font-(?:family|weight)\\s*:[^;]*(?:;|$)", "");

        node.setStyle(existingStyle + ";" + getFontStyle());
    }

    /**
     * Updates labels and checkboxes displayed on the theme background.
     *
     * @param node the node to style
     */
    private static void applyTextColor(final Node node) {
        if (!(node instanceof Label) && !(node instanceof CheckBox)) {
            return;
        }

        if (isInsideSurfaceControl(node)) {
            return;
        }

        final ARGBColor color = Theme.getReadableTextColor(currentTheme.getPrimaryColor());

        final String existingStyle =
                node.getStyle().replaceAll("(?i)-fx-text-fill\\s*:[^;]*(?:;|$)", "");

        node.setStyle(
                existingStyle
                        + ";"
                        + String.format(
                                Locale.ROOT,
                                "-fx-text-fill: rgb(%d,%d,%d);",
                                color.red(),
                                color.green(),
                                color.blue()));
    }

    /**
     * Checks whether a node belongs to a control with its own background.
     *
     * @param node the node to inspect
     * @return true if an ancestor manages its own text contrast
     */
    private static boolean isInsideSurfaceControl(final Node node) {
        Parent parent = node.getParent();

        while (parent != null) {
            if (parent instanceof ButtonBase
                    || parent instanceof ComboBoxBase<?>
                    || parent instanceof ListCell<?>
                    || parent instanceof ListView<?>
                    || parent instanceof TableView<?>
                    || parent instanceof TreeView<?>
                    || parent instanceof TitledPane) {
                return true;
            }

            parent = parent.getParent();
        }

        return false;
    }

    /**
     * Applies the active primary background color.
     *
     * @param node the node to style
     */
    private static void applyBackground(final Node node) {
        final String existingStyle =
                node.getStyle().replaceAll("(?i)-fx-background-color\\s*:[^;]*(?:;|$)", "");

        node.setStyle(existingStyle + ";" + getPrimaryBackgroundStyle());
    }

    /**
     * Returns font family and weight declarations for the active theme.
     *
     * @return CSS declarations without a font size
     */
    public static String getFontStyle() {
        final Theme theme = getTheme();

        final String fontFamily = theme.getFontFamily().replace("\\", "\\\\").replace("'", "\\'");

        return "-fx-font-family: '"
                + fontFamily
                + "';"
                + "-fx-font-weight: "
                + (theme.isBoldText() ? "bold;" : "normal;");
    }

    /**
     * Returns the primary background color as CSS.
     *
     * @return the background color declaration
     */
    public static String getPrimaryBackgroundStyle() {
        final ARGBColor color = getTheme().getPrimaryColor();

        return String.format(
                Locale.ROOT,
                "-fx-background-color: rgba(%d,%d,%d,%.4f);",
                color.red(),
                color.green(),
                color.blue(),
                color.alpha() / MAX_COLOR_COMPONENT);
    }
}
