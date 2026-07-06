package it.unibo.unibodget.model.currency.bank;

import java.math.BigDecimal;

/**
 * Represents the result of a bank‑mediated currency conversion.
 * <p>
 * A {@code BankConversionResult} contains three financial values:
 * <ul>
 *     <li><strong>convertedAmount</strong> — the amount received in the target currency</li>
 *     <li><strong>commission</strong> — the total fee charged by the bank, expressed in the source currency</li>
 *     <li><strong>totalCost</strong> — the full amount paid by the user in the source currency,
 *         including commission</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 *     <li>The converted amount is always expressed in the <em>target</em> currency.</li>
 *     <li>The commission and total cost are expressed in the <em>source</em> currency,
 *         because fees are applied before conversion.</li>
 *     <li>Currency codes are stored as strings to avoid coupling with higher‑level
 *         currency models.</li>
 * </ul>
 */
public final class BankConversionResult {

    private final BigDecimal convertedAmount;
    private final BigDecimal commission;
    private final BigDecimal totalCost;
    private final String sourceCurrencyCode;
    private final String targetCurrencyCode;

    /**
     * Creates a new {@code BankConversionResult}.
     *
     * @param convertedAmount     the amount obtained after conversion, in the target currency
     * @param commission          the total commission charged by the bank, in the source currency
     * @param totalCost           the full cost paid by the user, in the source currency
     * @param sourceCurrencyCode  the ISO currency code of the source currency (e.g., "EUR")
     * @param targetCurrencyCode  the ISO currency code of the target currency (e.g., "USD")
     */
    public BankConversionResult(BigDecimal convertedAmount,
                                BigDecimal commission,
                                BigDecimal totalCost,
                                String sourceCurrencyCode,
                                String targetCurrencyCode) {
        this.convertedAmount = convertedAmount;
        this.commission = commission;
        this.totalCost = totalCost;
        this.sourceCurrencyCode = sourceCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
    }

    /**
     * Returns the converted amount in the target currency.
     *
     * @return the converted amount
     */
    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    /**
     * Returns the commission charged by the bank, expressed in the source currency.
     *
     * @return the commission amount
     */
    public BigDecimal getCommission() {
        return commission;
    }

    /**
     * Returns the total cost paid by the user, expressed in the source currency.
     *
     * @return the total cost
     */
    public BigDecimal getTotalCost() {
        return totalCost;
    }

    /**
     * Returns the ISO code of the source currency.
     *
     * @return the source currency code
     */
    public String getSourceCurrencyCode() {
        return sourceCurrencyCode;
    }

    /**
     * Returns the ISO code of the target currency.
     *
     * @return the target currency code
     */
    public String getTargetCurrencyCode() {
        return targetCurrencyCode;
    }
}
