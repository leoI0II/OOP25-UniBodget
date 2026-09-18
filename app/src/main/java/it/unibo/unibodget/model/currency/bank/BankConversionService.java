package it.unibo.unibodget.model.currency.bank;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import java.math.BigDecimal;

/**
 * Service responsible for performing a bank‑mediated currency conversion.
 * 
 * <p>
 * A {@code BankConversionService} combines:
 * <ul>
 *     <li>a base currency conversion performed by {@link BasicCurrencyConverter}</li>
 *     <li>a commission calculation performed by {@link BankCalculator}</li>
 * </ul>
 * 
 * <p>
 * The result is returned as a {@link BankConversionResult}, containing:
 * <ul>
 *     <li>the converted amount in the target currency</li>
 *     <li>the commission charged by the bank (source currency)</li>
 *     <li>the total cost paid by the user (source currency)</li>
 * </ul>
 */
public class BankConversionService {

    private final BasicCurrencyConverter converter;

    /**
     * Creates a new {@code BankConversionService}.
     *
     * @param converter the base currency converter used to compute the raw conversion;
     *                  must not be {@code null}
     */
    public BankConversionService(final BasicCurrencyConverter converter) {
        this.converter = converter;
    }

    /**
     * Performs a full bank‑mediated conversion.
     * 
     * <p>
     * Steps:
     * <ol>
     *     <li>Convert the requested amount using the base converter.</li>
     *     <li>Compute the bank commission using {@link BankCalculator}.</li>
     *     <li>Compute the total cost paid by the user (amount + commission).</li>
     *     <li>Return a {@link BankConversionResult} containing all values.</li>
     * </ol>
     *
     * @param amount the amount to convert, expressed in the source currency
     * @param from   the source currency
     * @param to     the target currency
     * @param bank   the bank whose fee structure should be applied
     * @return a {@link BankConversionResult} containing converted amount, commission, and total cost
     */
    public BankConversionResult convert(final BigDecimal amount, final CurrencyUnit from, 
                                        final CurrencyUnit to, final Bank bank) {
        final CurrencyConversionResult baseResult = converter.convert(amount, from, to);
        final BigDecimal commission = BankCalculator.calculateCommission(amount, bank);
        final BigDecimal totalCost = amount.add(commission);

        return new BankConversionResult(
                baseResult.getConvertedAmount(),
                commission,
                totalCost,
                from.getCode(),
                to.getCode()
        );
    }
}
