package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ThemeManagerTest {

    @Test
    void shouldSetAndGetTheme() {
        final Theme t = new Theme("X", "#FFFFFF", "#000000");
        ThemeManager.setTheme(t);

        assertEquals(t, ThemeManager.getTheme());
    }

}
