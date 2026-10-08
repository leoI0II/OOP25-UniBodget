package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.settings.WindowPreferences;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Standalone JavaFX launcher for testing the Currency Converter dashboard
 * outside the full UniBodget application.
 *
 * <p>Loads saved settings, initializes the converter controller,
 * applies the current theme and displays the converter dashboard.</p>
 */
public final class TestCurrencyConverterApp extends Application {

    /** {@inheritDoc} */
    @Override
    public void start(final Stage stage) {
        final SettingsController settingsController = new SettingsController();
        final WindowPreferences savedPrefs =
                settingsController.getSettings().getWindowPrefs();

        final CurrencyConverterController controller =
                CurrencyConverterFactory.create();

        final Parent root = CurrencyConverterViewFX.buildContent(controller);
        final Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Currency Converter");
        stage.setWidth(savedPrefs.getWidth());
        stage.setHeight(savedPrefs.getHeight());
        stage.setMaximized(savedPrefs.isMaximized());

        ThemeManager.applyThemeToScene(scene);
        stage.show();

        // Persist normal window dimensions without storing maximized dimensions.
        stage.widthProperty().addListener((obs, oldValue, newValue) -> {
            if (!stage.isMaximized()) {
                final WindowPreferences prefs =
                        settingsController.getSettings().getWindowPrefs();

                settingsController.updateWindowPrefs(new WindowPreferences(
                        newValue.doubleValue(),
                        prefs.getHeight(),
                        prefs.isMaximized()
                ));
            }
        });

        stage.heightProperty().addListener((obs, oldValue, newValue) -> {
            if (!stage.isMaximized()) {
                final WindowPreferences prefs =
                        settingsController.getSettings().getWindowPrefs();

                settingsController.updateWindowPrefs(new WindowPreferences(
                        prefs.getWidth(),
                        newValue.doubleValue(),
                        prefs.isMaximized()
                ));
            }
        });

        stage.maximizedProperty().addListener((obs, oldValue, newValue) -> {
            final WindowPreferences prefs =
                    settingsController.getSettings().getWindowPrefs();

            settingsController.updateWindowPrefs(new WindowPreferences(
                    prefs.getWidth(),
                    prefs.getHeight(),
                    newValue
            ));
        });
    }

    /**
     * Launches the standalone Currency Converter dashboard.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        launch(args);
    }
}
