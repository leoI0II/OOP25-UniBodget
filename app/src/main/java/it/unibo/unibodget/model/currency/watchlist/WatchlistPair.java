package it.unibo.unibodget.model.currency.watchlist;

import java.util.Objects;

/**
 * Represents a preset currency conversion pair stored in the user's watchlist.
 * 
 * <p>
 * A {@code WatchlistPair} defines a frequently used conversion direction,
 * such as EUR → USD, allowing the UI to quickly recall preferred currency
 * pairs without requiring the user to manually reselect them each time.
 * 
 * <p>
 * This record is immutable and acts as a value object within the currency
 * converter module. Two pairs are considered equal if both their {@code from}
 * and {@code to} currencies match.
 *
 * @param from the base currency of the conversion (left side of the pair)
 * @param to   the target currency of the conversion (right side of the pair)
 * @throws IllegalArgumentException if {@code from} and {@code to} are the same currency
 * @throws NullPointerException     if either parameter is null
 */
public record WatchlistPair(String from, String to) {

    /**
     * Constructor used to validate the pair.
     */
    public WatchlistPair {
        Objects.requireNonNull(from, "Base currency cannot be null");
        Objects.requireNonNull(to, "Target currency cannot be null");

        if (from.equals(to)) {
            throw new IllegalArgumentException("Watchlist pair cannot contain identical currencies");
        }
    }

    /**
     * Returns a human-readable representation of the conversion pair,
     * formatted as "FROM ➔ TO", using the ISO currency codes.
     *
     * @return a string representation of the pair, e.g. "EUR ➔ USD"
     */
    @Override
    public String toString() {
        return from + " ➔ " + to;
    }

    /**
     * Ensures consistent equality semantics for use in sets or maps.
     *
     * @return true if both the base and target currencies match
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof WatchlistPair other)) {
            return false;
        }
        return from.equals(other.from) && to.equals(other.to);
    }

    /**
     * Computes a stable hash code based on both currencies.
     *
     * @return the hash code for this pair
     */
    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
