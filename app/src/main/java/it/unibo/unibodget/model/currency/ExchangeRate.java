package it.unibo.unibodget.model.currency;

import java.time.Instant;

/**
 * Represents a single exchange rate between two currencies at a specific moment in time.
 * This class is immutable and contains the base currency, the target currency,
 * the numerical exchange rate, and the timestamp of when the rate was retrieved.
 */
public class ExchangeRate {

    private final CurrencyUnit base;
    private final CurrencyUnit target;
    private final double rate;
    private final Instant timestamp;

    /**
     * Creates a new ExchangeRate instance.
     *
     * @param base      the base currency from which the conversion starts
     * @param target    the target currency to which the conversion is applied
     * @param rate      the exchange rate value (target per base)
     * @param timestamp the moment when the rate was obtained
     */
    public ExchangeRate(CurrencyUnit base, CurrencyUnit target, double rate, Instant timestamp) {
        this.base = base;
        this.target = target;
        this.rate = rate;
        this.timestamp = timestamp;
    }

    /** @return the base currency */
    public CurrencyUnit getBase() {
        return base;
    }

    /** @return the target currency */
    public CurrencyUnit getTarget() {
        return target;
    }

    /** @return the exchange rate value */
    public double getRate() {
        return rate;
    }

    /** @return the timestamp of the rate */
    public Instant getTimestamp() {
        return timestamp;
    }
}
