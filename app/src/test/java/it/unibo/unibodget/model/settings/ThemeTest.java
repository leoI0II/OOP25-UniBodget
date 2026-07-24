package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.utils.ARGBColor;

class ThemeTest {

    @Test
    void shouldComputeReadableTextColor() {
        final ARGBColor bright = new ARGBColor("#FFFFFF");
        final ARGBColor dark = new ARGBColor("#000000");

        assertEquals(ARGBColor.BLACK, Theme.getReadableTextColor(bright));
        assertEquals(ARGBColor.WHITE, Theme.getReadableTextColor(dark));
    }

    @Test
    void shouldFallbackToDefaultFontSize() {
        final Theme t = 
            new Theme("X", ARGBColor.WHITE, ARGBColor.BLACK, ARGBColor.BLACK, "Arial", -5, false);
        assertEquals(Theme.DEFAULT.getFontSize(), t.getFontSize());
    }

    @Test
    void shouldStoreAllFields() {
        final Theme t = new Theme("Test", "#FFFFFF", "#000000");

        assertEquals("Test", t.getName());
        assertEquals("Arial", t.getFontFamily());
        assertEquals(14, t.getFontSize());
    }

    @Test
    void shouldCompareThemesCorrectly() {
        final Theme t1 = new Theme("A", "#FFFFFF", "#000000");
        final Theme t2 = new Theme("A", "#FFFFFF", "#000000");

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

}
