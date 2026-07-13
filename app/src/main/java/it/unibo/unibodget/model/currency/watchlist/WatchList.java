package it.unibo.unibodget.model.currency.watchlist;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibo.unibodget.persistency.ModelFileManager;

/**
 * Manages the collection of user-preferred currencies.
 * This model maintains the state of the watchlist and provides 
 * methods for modification.
 */
public final class WatchList {

    private static final Path PATH = Path.of("data/json/currency/watchlist/Watchlist.json");
    private static final String RESOURCE = "/json/currency/watchlist/Watchlist.json";

    private static boolean initialized = false;
    private static final Set<WatchlistPair> LOADED = new HashSet<>();

    public WatchList() {
        if (!initialized) {
            init();
        }
    }

    /**
     * Adds a currency to the user's watchlist.
     * @param pair the watchlist pair to add.
     */
    public boolean add(final WatchlistPair pair) {
        final boolean added = LOADED.add(pair);
        if (added){
            save();
        }
        return added;
    }

    /**
     * Removes a currency from the user's watchlist.
     * @param pair the watchlist pair to remove.
     */
    public boolean remove(final WatchlistPair pair) {
        final boolean removed = LOADED.remove(pair);
        if (removed){
            save();
        }
        return removed;
    }

    /**
     * Returns an unmodifiable view of the current watchlist.
     * 
     * @return a set of preferred currencies.
     */
    public Set<WatchlistPair> getFavorites() {
        return Collections.unmodifiableSet(LOADED);
    }

    /**
     * Saves the current watchlist to the JSON file.
     */
    private static void save() {
        try {
            final ModelFileManager<WatchlistPair> mgr =
                new ModelFileManager<>(PATH, RESOURCE, WatchlistPair.class);
            mgr.open();
            mgr.saveList("watchlist", new ArrayList<>(LOADED));
            mgr.close();
        } catch (final Exception e) {
            System.out.println("WatchList save failed");
        }
    }

    /**
     * Initializes the watchlist by loading data from the JSON file.
     */
    public static void init() {
        try {
            final ModelFileManager<WatchlistPair> mgr =
                new ModelFileManager<>(PATH, RESOURCE, WatchlistPair.class);
            mgr.open();
            // load from json
            var list = mgr.loadList("watchlist");
            mgr.close();
            if (list == null || list.isEmpty()) {
                System.out.println("WatchList JSON empty → using empty list");
                LOADED.clear();
                LOADED.addAll(generateMockWatchlist());
            } else {
                LOADED.clear();
                LOADED.addAll(list);
                System.out.println("WatchList loaded from JSON → " + LOADED.size() + " items");
            }
            initialized = true;
        } catch (final Exception e) {
            System.out.println("WatchList load failed → using empty list");
            LOADED.clear();
            LOADED.addAll(generateMockWatchlist());
            initialized = true;
        }
    }

    /**
     * Generates a mock watchlist for testing purposes.
     * 
     * @return a list of sample watchlist pairs.
     */
    private static List<WatchlistPair> generateMockWatchlist() {
        return List.of(
            new WatchlistPair("EUR", "USD"),
            new WatchlistPair("EUR", "GBP"),
            new WatchlistPair("USD", "JPY")
        );
    }

}
