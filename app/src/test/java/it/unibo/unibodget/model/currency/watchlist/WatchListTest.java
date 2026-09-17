package it.unibo.unibodget.model.currency.watchlist;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WatchListTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";

    @BeforeEach
    void resetWatchList() throws Exception {
        final Field loadedField = WatchList.class.getDeclaredField("LOADED");
        loadedField.setAccessible(true);
        ((Set<?>) loadedField.get(null)).clear();

        final Field initField = WatchList.class.getDeclaredField("initialized");
        initField.setAccessible(true);
        initField.set(null, true); // evita init()
    }

    @Test
    void shouldAddPair() {
        final WatchList wl = new WatchList();

        final WatchlistPair p = new WatchlistPair(EUR, USD);

        assertTrue(wl.add(p));
        assertTrue(wl.getFavorites().contains(p));
    }

    @Test
    void shouldNotAddDuplicate() {
        final WatchList wl = new WatchList();

        final WatchlistPair p = new WatchlistPair(EUR, USD);

        assertTrue(wl.add(p));
        assertFalse(wl.add(p));
    }

    @Test
    void shouldRemovePair() {
        final WatchList wl = new WatchList();

        final WatchlistPair p = new WatchlistPair(EUR, USD);

        wl.add(p);
        assertTrue(wl.remove(p));
        assertFalse(wl.getFavorites().contains(p));
    }

}
