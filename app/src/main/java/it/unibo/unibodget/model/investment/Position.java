package it.unibo.unibodget.model.investment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Represents a snapshot of an open position on a single investment asset.
 *
 * <p>A position is always derived from the transaction history; it is never
 * stored as state. The fields capture the current state at the moment of
 * computation.
 *
 * This class is a record, which provides immutability, value-based equality, and a concise syntax.
 * 
 * @param asset                the asset held (ex. AAPL, BTC)
 * @param quantity             the net quantity currently held
 * @param averageBasisCost     the weighted average cost per unit (in baseCurrency)
 * @param currentMarketValue   the current market value of the position (in baseCurrency)
 */
public record Position(
    CurrencyUnit asset,
    BigDecimal quantity,
    Asset averageBasisCost,
    Asset currentMarketValue
) {
    
    /**
     * @throws NullPointerException if any parameter is null
     */
    public Position {
        Objects.requireNonNull(asset);
        Objects.requireNonNull(quantity);
        Objects.requireNonNull(averageBasisCost);
        Objects.requireNonNull(currentMarketValue);
    }

    /**
     * Gets the total cost basis of the position, calculated as quantity × average basis cost.
     * 
     * @return the total cost basis of the position in the base currency
     */
    public Asset getTotalCost() {
        return averageBasisCost.multiply(quantity);
    }

    /**
     * Gets the profit or loss of the position, calculated as current market value − total cost.
     * 
     * @return the profit or loss of the position in the base currency
     */
    public Asset getProfitLoss() {
        return currentMarketValue.subtract(getTotalCost());
    }

    /**
     * Returns the unrealized profit or loss as a percentage of the total cost basis.
     * Returns {@code 0} if the total cost basis is zero to avoid division by zero.
     *
     * @return the P/L percentage (e.g. 12.5 means +12.5%, -5.0 means -5.0%)
     */
    public BigDecimal getProfitLossPercentage() {
        if (getTotalCost().isZero()) {
            return BigDecimal.ZERO; // Avoid division by zero; define as 0% if no cost basis
        }
        return getProfitLoss()
            .amount()
            .divide(getTotalCost().amount(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Returns whether this position is considered closed.
     * A position is closed if the net quantity is zero (fully sold) or negative (net short).
     *
     * @return {@code true} if the position is closed or net short, {@code false} otherwise
     */
    public boolean isClosedPosition() {
        boolean isZero = quantity.compareTo(BigDecimal.ZERO) == 0;
        boolean isNegative = quantity.compareTo(BigDecimal.ZERO) < 0;
        return isZero || isNegative; // A position is considered closed if quantity is zero or negative (net short)
    }
}
