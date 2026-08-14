package it.unibo.unibodget.controller.currency_converter;

import it.unibo.unibodget.model.currency.watchlist.WatchList;
import it.unibo.unibodget.model.currency.watchlist.WatchlistPair;
import java.util.Set;

/**
 * Controller responsible for managing the user's watchlist logic.
 */
public final class WatchListController {

    private final WatchList model;

    /**
     * Creates a controller that manages a {@link WatchList}.
     *
     * @param model the watchlist model to operate on
     */
    public WatchListController(final WatchList model) {
        this.model = model;
    }

    /**
     * Attempts to add a currency pair to the watchlist.
     * 
     * @param pair the pair to add
     * @return true if the pair was added, false if it already existed
     */
    public boolean addPair(final WatchlistPair pair) {
        if (pair == null) {
            return false;
        }
        return model.add(pair);
    }

    /**
     * Removes a currency pair from the watchlist.
     * 
     * @param pair the pair to remove
     */
    public void removePair(final WatchlistPair pair) {
        model.remove(pair);
    }

    /**
     * Returns the current set of saved pairs.
     * 
     * @return an unmodifiable set of WatchlistPair
     */
    public Set<WatchlistPair> getFavorites() {
        return model.getFavorites();
    }
}
