package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ThemeManagerTest {

    @Test
    void shouldSetAndGetTheme() {
        Theme t = new Theme("X", "#FFFFFF", "#000000");
        ThemeManager.setTheme(t);

        assertEquals(t, ThemeManager.getTheme());
    }

}
