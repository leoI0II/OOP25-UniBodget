package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.controller.settings.SettingsController;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

/**
 * UI component that provides the "Settings" tab inside the main application.
 *
 * <p>The tab contains a single button that opens the settings popup
 * managed by {@link SettingsPopupFX}. This class acts as a lightweight
 * bridge between the controller and the JavaFX view layer.</p>
 */
public final class SettingsTabFX {

    private final SettingsController controller;

    /**
     * Creates a new SettingsTabFX bound to the given controller.
     *
     * @param controller the settings controller used to load and save preferences
     */
    public SettingsTabFX(final SettingsController controller) {
        this.controller = controller;
    }

    /**
     * Builds the Settings tab and attaches the popup-opening button.
     *
     * @param owner the main application window, used as the parent for the popup
     * @return a non-closable {@link Tab} containing the settings button
     */
    public Tab create(final Stage owner) {

        // Create the tab shown in the main UI
        final Tab tab = new Tab("Settings");

        // Button that opens the popup dialog
        final Button openPopup = new Button("Settings");

        // When clicked → show the popup
        openPopup.setOnAction(e -> new SettingsPopupFX(controller).show(owner));

        // Add button to tab
        tab.setContent(openPopup);

        // Prevent closing the tab
        tab.setClosable(false);

        return tab;
    }
}
