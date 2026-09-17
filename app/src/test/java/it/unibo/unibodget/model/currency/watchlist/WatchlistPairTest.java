package it.unibo.unibodget.model.currency.watchlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WatchlistPairTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";

    @Test
    void shouldStoreFieldsCorrectly() {
        final WatchlistPair p = new WatchlistPair(EUR, USD);

        assertEquals(EUR, p.from());
        assertEquals(USD, p.to());
    }

    @Test
    void shouldRejectIdenticalCurrencies() {
        assertThrows(IllegalArgumentException.class, () ->
                new WatchlistPair(EUR, EUR)
        );
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        final WatchlistPair p1 = new WatchlistPair(EUR, USD);
        final WatchlistPair p2 = new WatchlistPair(EUR, USD);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void shouldPrintReadableToString() {
        final WatchlistPair p = new WatchlistPair(EUR, USD);

        assertEquals("EUR ➔ USD", p.toString());
    }

}
