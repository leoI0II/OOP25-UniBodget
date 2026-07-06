package it.unibo.unibodget.model.currency.alert;

import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Represents a threshold-based alert for a specific currency pair.
 * <p>
 * A {@code CurrencyAlert} monitors the exchange rate between two currencies
 * and triggers when the current rate crosses a predefined threshold.
 * The direction of the trigger (above or below the threshold) is controlled
 * by the {@code isLowerThan} flag.
 * <p>
 * Alerts are strictly pair-specific: the {@link #check(double, CurrencyUnit, CurrencyUnit)}
 * method only evaluates the threshold if the provided currency pair matches
 * the one defined in this alert.
 */
public final class CurrencyAlert {

    private final CurrencyUnit fromCurrency;
    private final CurrencyUnit toCurrency;
    private final double threshold;
    private final boolean isLowerThan; // true → alert triggers when rate < threshold

    /**
     * Creates a new {@code CurrencyAlert} for a specific currency pair.
     *
     * @param fromCurrency the base currency of the monitored pair; must not be {@code null}
     * @param toCurrency   the target currency of the monitored pair; must not be {@code null}
     * @param threshold    the numeric threshold to compare against
     * @param isLowerThan  determines the trigger direction:
     *                     <ul>
     *                         <li>{@code true} → alert triggers when {@code currentRate < threshold}</li>
     *                         <li>{@code false} → alert triggers when {@code currentRate > threshold}</li>
     *                     </ul>
     */
    public CurrencyAlert(CurrencyUnit fromCurrency, CurrencyUnit toCurrency,
                         double threshold, boolean isLowerThan) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.threshold = threshold;
        this.isLowerThan = isLowerThan;
    }

    /**
     * Evaluates whether the alert should trigger based on the current exchange rate.
     * <p>
     * Behavior:
     * <ul>
     *     <li>If the provided currency pair does not match the alert's pair,
     *         the method returns {@code false}.</li>
     *     <li>If the pair matches, the method compares {@code currentRate}
     *         with the configured threshold.</li>
     *     <li>The comparison direction depends on {@code isLowerThan}.</li>
     * </ul>
     *
     * @param currentRate the current exchange rate to evaluate
     * @param from        the base currency of the rate being checked
     * @param to          the target currency of the rate being checked
     * @return {@code true} if the alert conditions are met; {@code false} otherwise
     */
    public boolean check(double currentRate, CurrencyUnit from, CurrencyUnit to) {
        System.out.println("Confronto: " + this.fromCurrency.getCode() + "==" + from.getCode());
        if (!this.fromCurrency.equals(from) || !this.toCurrency.equals(to)) {
            System.out.println("Currency mismatch");
            return false;
        }
        return isLowerThan ? currentRate < threshold : currentRate > threshold;
    }

    /**
     * Returns the target currency of the monitored pair.
     *
     * @return the target {@link CurrencyUnit}
     */
    public CurrencyUnit getTargetCurrency() {
        return toCurrency;
    }

    /**
     * Returns the base currency of the monitored pair.
     *
     * @return the source {@link CurrencyUnit}
     */
    public CurrencyUnit getSourceCurrency() {
        return fromCurrency;
    }
}
