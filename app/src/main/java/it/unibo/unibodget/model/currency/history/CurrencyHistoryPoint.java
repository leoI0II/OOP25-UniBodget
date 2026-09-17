package it.unibo.unibodget.model.currency.history;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a single historical exchange‑rate value for a currency pair
 * on a specific date.
 * 
 * <p>
 * This class is an immutable value object used primarily to feed chart
 * components, statistical views, or any UI element that requires a
 * time‑series representation of currency movements.
 * 
 * <p>
 * Typical usage:
 * <ul>
 *     <li>building line charts of exchange‑rate trends</li>
 *     <li>displaying historical data in tables or timelines</li>
 *     <li>feeding analytics modules that compute averages or volatility</li>
 * </ul>
 * 
 * <p>
 * Each instance stores:
 * <ul>
 *     <li>a {@link LocalDate} representing the day of the rate</li>
 *     <li>a {@code double} representing the exchange rate on that day
 *         (target currency per base currency)</li>
 * </ul>
 */
public final class CurrencyHistoryPoint {

    private final LocalDate date;
    private final double rate;

    /**
     * Creates a new historical exchange‑rate data point.
     *
     * @param date the date associated with the exchange rate; must not be {@code null}
     * @param rate the exchange rate value on that date (target per base)
     */
    public CurrencyHistoryPoint(final LocalDate date, final double rate) {
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        this.rate = rate;
    }

    /**
     * Returns the date of this historical data point.
     *
     * @return the date associated with the rate
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the exchange rate value for this date.
     *
     * @return the exchange rate (target per base)
     */
    public double getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return date + " -> " + rate;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof CurrencyHistoryPoint other
                && date.equals(other.date)
                && Double.compare(rate, other.rate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, rate);
    }
}
