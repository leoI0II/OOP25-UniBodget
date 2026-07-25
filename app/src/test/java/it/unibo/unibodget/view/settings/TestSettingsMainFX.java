package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.settings.WindowPreferences;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Standalone JavaFX launcher for testing the Settings dashboard outside
 * of the full UniBodget application.
 *
 * <p>
 * This utility class initializes the {@link SettingsController},
 * loads saved {@link WindowPreferences}, applies the current theme via
 * {@link ThemeManager}, and displays the settings UI using
 * {@link SettingsTabFX}.
 * </p>
 */
public final class TestSettingsMainFX extends Application {

    @Override
    public void start(final Stage stage) {

        final SettingsController controller = new SettingsController();
        final WindowPreferences savedPrefs = controller.getSettings().getWindowPrefs();

        final TabPane pane = new TabPane();
        pane.getTabs().add(new SettingsTabFX(controller).create(stage));

        final Scene scene = new Scene(pane, savedPrefs.getWidth(), savedPrefs.getHeight());
        stage.setScene(scene);
        stage.setTitle("Settings Manager");
        stage.setMaximized(savedPrefs.isMaximized());
        ThemeManager.applyThemeToScene(scene);

        stage.show();

        // Auto-update WindowPreferences
        stage.widthProperty().addListener((obs, oldV, newV) -> {
            controller.updateWindowPrefs(new WindowPreferences(
                    stage.getWidth(),
                    stage.getHeight(),
                    stage.isMaximized()
            ));
        });

        stage.heightProperty().addListener((obs, oldV, newV) -> {
            controller.updateWindowPrefs(new WindowPreferences(
                    stage.getWidth(),
                    stage.getHeight(),
                    stage.isMaximized()
            ));
        });

        stage.maximizedProperty().addListener((obs, oldV, newV) -> {
            controller.updateWindowPrefs(new WindowPreferences(
                    stage.getWidth(),
                    stage.getHeight(),
                    stage.isMaximized()
            ));
        });
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
