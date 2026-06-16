package it.unibo.unibodget.model.currency.watchlist;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages the collection of user-preferred currencies.
 * This model maintains the state of the watchlist and provides 
 * methods for modification.
 */
public final class WatchList {

    private final Set<WatchlistPair> favorites = new HashSet<>();

    /**
     * Adds a currency to the user's watchlist.
     * @param pair the watchlist pair to add.
     */
    public boolean add(WatchlistPair pair) {
        return favorites.add(pair);
    }

    /**
     * Removes a currency from the user's watchlist.
     * @param pair the watchlist pair to remove.
     */
    public boolean remove(WatchlistPair pair) {
        return favorites.remove(pair);
    }

    /**
     * Returns an unmodifiable view of the current watchlist.
     * @return a set of preferred currencies.
     */
    public Set<WatchlistPair> getFavorites() {
        return Collections.unmodifiableSet(favorites);
    }
}
