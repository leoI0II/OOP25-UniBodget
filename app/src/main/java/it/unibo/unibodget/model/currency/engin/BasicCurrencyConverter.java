package it.unibo.unibodget.model.currency.engin;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyType;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard implementation of {@link CurrencyConverter} that performs conversions
 * using exchange rates retrieved from an {@link ExchangeRateAPI}. All conversions
 * are computed relative to a fixed internal base currency, which simplifies
 * rate management and reduces the number of required API calls.
 */
public class BasicCurrencyConverter implements CurrencyConverter {

    private final int scale = 20;

    private final ExchangeRateAPI api;
    private final CurrencyUnit baseCurrency;

    /**
     * Creates a new {@code BasicCurrencyConverter}.
     * Excludes STOCK-type currencies from conversion capabilities,
     * and relies on the provided API to obtain exchange rates for supported currencies
     * checking taxes existence
     *
     * @param api the exchange-rate provider used to obtain conversion data
     * @param baseCurrency the internal base currency used for intermediate conversions
     */
    public BasicCurrencyConverter(final ExchangeRateAPI api, final CurrencyUnit baseCurrency) {
        this.api = api;
        this.baseCurrency = baseCurrency;
    }

    /**
     * Converts an amount from one currency to another using the exchange rates
     * provided by the {@link ExchangeRateAPI}. The conversion is performed relative
     * to the internal base currency.
     * 
     * @param amount the amount to convert
     * @param from the source currency unit
     * @param to the target currency unit
     * @return a {@link CurrencyConversionResult} containing the converted amount,
     *         the applied exchange rate, and the source/target currencies
     */
    @Override
    public CurrencyConversionResult convert(final BigDecimal amount, final CurrencyUnit from, final CurrencyUnit to) {
        if (from.getType() == CurrencyType.STOCK || to.getType() == CurrencyType.STOCK) {
            final String errorMsg = "Conversion involving STOCK-type currencies is not supported.";
            System.err.println(errorMsg);
            throw new IllegalArgumentException("Le valute di tipo STOCK non sono supportate per la conversione.");
        }

        if (from.equals(to)) {
            return new CurrencyConversionResult(amount, from, to, BigDecimal.ONE, amount);
        }

        final Map<CurrencyUnit, Double> rates = api.getLatestRates(baseCurrency);
        final Map<String, Double> normalized = new HashMap<>();
        rates.forEach((unit, value) -> normalized.put(unit.getCode(), value));

        if (!normalized.containsKey(from.getCode()) || !normalized.containsKey(to.getCode())) {
            System.out.println("1. Exchange rates not available for the selected currencies: "
                                + from.getCode() + " or " + to.getCode());
            throw new IllegalArgumentException("Tasso di cambio non disponibile per le valute selezionate.");
        }

        final BigDecimal fromRate = BigDecimal.valueOf(normalized.get(from.getCode()));
        final BigDecimal toRate = BigDecimal.valueOf(normalized.get(to.getCode()));

        final BigDecimal amountInBase = amount.divide(fromRate, scale, RoundingMode.HALF_UP);
        final BigDecimal converted = amountInBase.multiply(toRate)
            .setScale(10, RoundingMode.HALF_UP);

        final BigDecimal appliedRate = toRate.divide(fromRate, 10, RoundingMode.HALF_UP);

        return new CurrencyConversionResult(amount, from, to, appliedRate, converted);
    }
}
