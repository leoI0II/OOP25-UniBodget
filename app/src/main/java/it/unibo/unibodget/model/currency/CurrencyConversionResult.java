package it.unibo.unibodget.model.currency;

import java.math.BigDecimal;

/**
 * Represents the result of a currency conversion operation.
 * It contains the original amount, the converted amount,
 * the applied exchange rate, and the involved currencies.
 */
public class CurrencyConversionResult {

    private final BigDecimal amount;
    private final BigDecimal convertedAmount;
    private final BigDecimal appliedRate;
    private final CurrencyUnit from;
    private final CurrencyUnit to;

    /**
     * Creates a new conversion result.
     *
     * @param amount the amount before conversion
     * @param convertedAmount the resulting amount after conversion
     * @param appliedRate the exchange rate used for the conversion
     * @param from the source currency
     * @param to the target currency
     */
    public CurrencyConversionResult(BigDecimal amount, CurrencyUnit from, CurrencyUnit to,
                                    BigDecimal appliedRate, BigDecimal convertedAmount) {
        this.amount = amount;
        this.from = from;
        this.to = to;
        this.appliedRate = appliedRate;
        this.convertedAmount = convertedAmount;
    }

    /** @return the original amount */
    public BigDecimal getAmount() {
        return new BigDecimal(String.valueOf(amount));
    }

    /** @return the converted amount */
    public BigDecimal getConvertedAmount() {
        return new BigDecimal(String.valueOf(convertedAmount));
    }

    /** @return the applied exchange rate */
    public BigDecimal getAppliedRate() {
        return new BigDecimal(String.valueOf(appliedRate));
    }

    /** @return the source currency */
    public CurrencyUnit getFrom() {
        return from;
    }

    /** @return the target currency */
    public CurrencyUnit getTo() {
        return to;
    }
}
