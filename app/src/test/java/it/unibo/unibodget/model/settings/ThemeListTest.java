package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ThemeListTest {

    @Test
    void shouldLoadAtLeastDefaultTheme() {
        final ThemeList list = new ThemeList();
        assertFalse(list.getThemes().isEmpty());
    }

    @Test
    void shouldReturnImmutableList() {
        final ThemeList list = new ThemeList();
        assertThrows(UnsupportedOperationException.class, () -> {
            list.getThemes().add(Theme.DEFAULT);
        });
    }

}
