package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.utils.ARGBColor;

class ThemeTest {

    private static final String WHITE = "#FFFFFF";
    private static final String BLACK = "#000000";
    private static final String TEST = "Test";
    private static final String TEST_A = "A";
    private static final String FONT_ARIAL = "Arial";
    private static final int FONT_14 = 14;

    @Test
    void shouldComputeReadableTextColor() {
        final ARGBColor bright = new ARGBColor(WHITE);
        final ARGBColor dark = new ARGBColor(BLACK);

        assertEquals(ARGBColor.BLACK, Theme.getReadableTextColor(bright));
        assertEquals(ARGBColor.WHITE, Theme.getReadableTextColor(dark));
    }

    @Test
    void shouldFallbackToDefaultFontSize() {
        final Theme t = 
            new Theme("X", ARGBColor.WHITE, ARGBColor.BLACK, ARGBColor.BLACK, FONT_ARIAL, -5, false);
        assertEquals(Theme.DEFAULT.getFontSize(), t.getFontSize());
    }

    @Test
    void shouldStoreAllFields() {
        final Theme t = new Theme(TEST, WHITE, BLACK);

        assertEquals(TEST, t.getName());
        assertEquals(FONT_ARIAL, t.getFontFamily());
        assertEquals(FONT_14, t.getFontSize());
    }

    @Test
    void shouldCompareThemesCorrectly() {
        final Theme t1 = new Theme(TEST_A, WHITE, BLACK);
        final Theme t2 = new Theme(TEST_A, WHITE, BLACK);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

}
