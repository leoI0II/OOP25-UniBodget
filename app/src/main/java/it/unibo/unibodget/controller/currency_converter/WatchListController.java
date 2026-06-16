package it.unibo.unibodget.controller.currency_converter;

import it.unibo.unibodget.model.currency.watchlist.WatchList;
import it.unibo.unibodget.model.currency.watchlist.WatchlistPair;
import java.util.Set;

/**
 * Controller responsible for managing the user's watchlist logic.
 */
public final class WatchListController {

    private final WatchList model;

    public WatchListController(WatchList model) {
        this.model = model;
    }

    /**
     * Attempts to add a currency pair to the watchlist.
     * * @param pair the pair to add
     * @return true if the pair was added, false if it already existed
     */
    public boolean addPair(WatchlistPair pair) {
        if (pair == null) {
            return false;
        }
        return model.add(pair);
    }

    /**
     * Removes a currency pair from the watchlist.
     * * @param pair the pair to remove
     */
    public void removePair(WatchlistPair pair) {
        model.remove(pair);
    }

    /**
     * Returns the current set of saved pairs.
     * * @return an unmodifiable set of WatchlistPair
     */
    public Set<WatchlistPair> getFavorites() {
        return model.getFavorites();
    }
}