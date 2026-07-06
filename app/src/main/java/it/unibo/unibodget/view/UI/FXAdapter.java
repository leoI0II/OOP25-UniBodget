package it.unibo.unibodget.view.UI;

import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.utils.ARGBColor;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Generic adapter that converts model-layer objects into JavaFX UI types.
 * This keeps the model free from UI dependencies.
 */
public final class FXAdapter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FXAdapter() {
        // utility class
    }

    /**
     * Converts an ARGBColor from the model into a JavaFX Color.
     *
     * @param c the ARGBColor to convert
     * @return the corresponding JavaFX Color
     */
    public static Color toFXColor(final ARGBColor c) {
        return Color.rgb(c.red(), c.green(), c.blue(), c.alpha() / 255.0);
    }

    /**
     * Converts the typographic settings of a Theme into a JavaFX Font.
     *
     * @param theme the Theme to convert
     * @return a JavaFX Font instance
     */
    public static Font toFXFont(final Theme theme) {
        return Font.font(
                theme.getFontFamily(),
                theme.isBoldText() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.getFontSize()
        );
    }
}
