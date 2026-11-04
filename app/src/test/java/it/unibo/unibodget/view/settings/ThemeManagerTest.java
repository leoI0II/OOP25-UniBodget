package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.model.settings.Theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ThemeManagerTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> { });
        } catch (final IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    @Test
    void shouldSetAndGetTheme() {
        final Theme t = new Theme("X", "#FFFFFF", "#000000");
        ThemeManager.setTheme(t);

        assertEquals(t, ThemeManager.getTheme());
    }

    @Test
    void shouldNotPolluteChildNodesWithInlineFontStyleOnApplyThemeToScene() {
        ThemeManager.setTheme(Theme.DARK);

        final Label label = new Label("Wallet Name");
        label.getStyleClass().add("wallet-tile-name");
        final VBox root = new VBox(label);
        final Scene scene = new Scene(root);

        ThemeManager.applyThemeToScene(scene);

        assertTrue(root.getStyle().contains("-fx-font-family"));
        assertTrue(root.getStyleClass().contains("theme-dark"));
        assertFalse(
                label.getStyle().contains("-fx-font-weight"),
                "Child node should not have inline font-weight overriding stylesheet classes");
    }
}
