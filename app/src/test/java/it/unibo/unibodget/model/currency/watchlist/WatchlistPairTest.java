package it.unibo.unibodget.model.currency.watchlist;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WatchlistPairTest {

    @Test
    void shouldStoreFieldsCorrectly() {
        WatchlistPair p = new WatchlistPair("EUR", "USD");

        assertEquals("EUR", p.from());
        assertEquals("USD", p.to());
    }

    @Test
    void shouldRejectIdenticalCurrencies() {
        assertThrows(IllegalArgumentException.class, () ->
                new WatchlistPair("EUR", "EUR")
        );
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        WatchlistPair p1 = new WatchlistPair("EUR", "USD");
        WatchlistPair p2 = new WatchlistPair("EUR", "USD");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void shouldPrintReadableToString() {
        WatchlistPair p = new WatchlistPair("EUR", "USD");

        assertEquals("EUR ➔ USD", p.toString());
    }

}
