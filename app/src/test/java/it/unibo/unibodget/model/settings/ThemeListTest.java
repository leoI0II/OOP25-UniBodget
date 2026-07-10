package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ThemeListTest {

    @Test
    void shouldLoadAtLeastDefaultTheme() {
        ThemeList list = new ThemeList();
        assertFalse(list.getThemes().isEmpty());
    }

    @Test
    void shouldReturnImmutableList() {
        ThemeList list = new ThemeList();
        assertThrows(UnsupportedOperationException.class, () -> {
            list.getThemes().add(Theme.DEFAULT);
        });
    }

}
