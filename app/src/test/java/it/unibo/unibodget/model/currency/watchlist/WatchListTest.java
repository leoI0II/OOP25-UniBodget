package it.unibo.unibodget.model.currency.watchlist;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WatchListTest {

    @BeforeEach
    void resetWatchList() throws Exception {
        Field loadedField = WatchList.class.getDeclaredField("LOADED");
        loadedField.setAccessible(true);
        ((Set<?>) loadedField.get(null)).clear();

        Field initField = WatchList.class.getDeclaredField("initialized");
        initField.setAccessible(true);
        initField.set(null, true); // evita init()
    }

    @Test
    void shouldAddPair() {
        WatchList wl = new WatchList();

        WatchlistPair p = new WatchlistPair("EUR", "USD");

        assertTrue(wl.add(p));
        assertTrue(wl.getFavorites().contains(p));
    }

    @Test
    void shouldNotAddDuplicate() {
        WatchList wl = new WatchList();

        WatchlistPair p = new WatchlistPair("EUR", "USD");

        assertTrue(wl.add(p));
        assertFalse(wl.add(p));
    }

    @Test
    void shouldRemovePair() {
        WatchList wl = new WatchList();

        WatchlistPair p = new WatchlistPair("EUR", "USD");

        wl.add(p);
        assertTrue(wl.remove(p));
        assertFalse(wl.getFavorites().contains(p));
    }

}
