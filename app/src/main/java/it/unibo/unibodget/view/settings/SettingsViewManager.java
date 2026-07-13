package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.settings.WindowPreferences;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Utility class responsible for opening the standalone Settings window.
 *
 * <p>The window loads the user's saved {@link WindowPreferences},
 * applies the current theme, and attaches automatic listeners to
 * persist window size and maximization state whenever the user
 * resizes or toggles fullscreen.</p>
 *
 * <p>This class is stateless and exposes only a single static method.</p>
 */
public final class SettingsViewManager {

    private SettingsViewManager() {
        // Prevent instantiation
    }

    /**
     * Opens the Settings window as a standalone JavaFX stage.
     *
     * <p>Steps performed:</p>
     * <ol>
     *     <li>Create a new {@link SettingsController}</li>
     *     <li>Load saved window preferences (size + maximized)</li>
     *     <li>Create a {@link TabPane} containing the Settings tab</li>
     *     <li>Apply theme to the scene</li>
     *     <li>Attach listeners to persist window changes automatically</li>
     * </ol>
     *
     * @param stage the stage to configure and display
     */
    public static void openSettings(final Stage stage) {

        // Controller used to load and save settings
        final SettingsController controller = new SettingsController();

        // Load previously saved window preferences
        final WindowPreferences savedPrefs = controller.getSettings().getWindowPrefs();

        // Build UI
        final TabPane pane = new TabPane();
        pane.getTabs().add(
                new SettingsTabFX(controller).create(stage)
        );

        // Create scene using saved width/height
        final Scene scene = new Scene(
                pane,
                savedPrefs.getWidth(),
                savedPrefs.getHeight()
        );

        stage.setScene(scene);
        stage.setTitle("Settings Manager");
        stage.setMaximized(savedPrefs.isMaximized());

        // Apply theme to the new scene
        ThemeManager.applyThemeToScene(scene);

        /*
         * Window Preferences
         *
         * Every time the user resizes or maximizes the window,
         * we persist the new values immediately.
         */

        stage.widthProperty().addListener((obs, oldV, newV) -> {
            controller.updateWindowPrefs(
                    new WindowPreferences(
                            stage.getWidth(),
                            stage.getHeight(),
                            stage.isMaximized()
                    )
            );
        });

        stage.heightProperty().addListener((obs, oldV, newV) -> {
            controller.updateWindowPrefs(
                    new WindowPreferences(
                            stage.getWidth(),
                            stage.getHeight(),
                            stage.isMaximized()
                    )
            );
        });

        stage.maximizedProperty().addListener((obs, oldV, newV) -> {
            controller.updateWindowPrefs(
                    new WindowPreferences(
                            stage.getWidth(),
                            stage.getHeight(),
                            stage.isMaximized()
                    )
            );
        });

        // Show the window
        stage.show();
    }
}
