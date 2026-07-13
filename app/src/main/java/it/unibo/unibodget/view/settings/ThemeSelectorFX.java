package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeList;
import javafx.scene.control.ComboBox;

/**
 * Factory class that creates a ComboBox for selecting a {@link Theme}.
 *
 * <p>The selector is populated with all themes provided by
 * {@link ThemeList}, including default and user‑defined themes.</p>
 */
public final class ThemeSelectorFX {

    private ThemeSelectorFX() {
        // Prevent instantiation
    }

    /**
     * Creates a ComboBox containing all available themes.
     *
     * @return a ComboBox populated with {@link Theme} entries
     */
    public static ComboBox<Theme> createThemeSelector() {

        // Create the selector
        final ComboBox<Theme> box = new ComboBox<>();

        // Load all themes from ThemeList
        final ThemeList list = new ThemeList();
        box.getItems().addAll(list.getThemes());

        return box;
    }
}
