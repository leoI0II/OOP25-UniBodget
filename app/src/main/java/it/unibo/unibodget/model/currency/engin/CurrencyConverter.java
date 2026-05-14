package it.unibo.unibodget.model.currency.engin;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Defines the operations required to convert monetary amounts between currencies.
 * Implementations are expected to rely on exchange-rate providers to obtain
 * the necessary conversion data.
 */
public interface CurrencyConverter {

    /**
     * Converts an amount from one currency to another using the most recent
     * exchange-rate information available.
     *
     * @param amount the amount to convert; must be non-negative
     * @param from the source currency
     * @param to the target currency
     * @return a {@link CurrencyConversionResult} describing the conversion outcome
     */
    CurrencyConversionResult convert(BigDecimal amount, CurrencyUnit from, CurrencyUnit to);
}
