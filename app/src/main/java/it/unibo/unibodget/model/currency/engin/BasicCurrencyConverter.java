package it.unibo.unibodget.model.currency.engin;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyType;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Standard implementation of {@link CurrencyConverter} that performs conversions
 * using exchange rates retrieved from an {@link ExchangeRateAPI}. All conversions
 * are computed relative to a fixed internal base currency, which simplifies
 * rate management and reduces the number of required API calls.
 */
public class BasicCurrencyConverter implements CurrencyConverter {

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
    public BasicCurrencyConverter(ExchangeRateAPI api, CurrencyUnit baseCurrency) {
        this.api = api;
        this.baseCurrency = baseCurrency;
    }

    @Override
    public CurrencyConversionResult convert(BigDecimal amount, CurrencyUnit from, CurrencyUnit to) {
        if (from.getType() == CurrencyType.STOCK || to.getType() == CurrencyType.STOCK) {
            throw new IllegalArgumentException("Le valute di tipo STOCK non sono supportate per la conversione.");
        }

        if (from.equals(to)) {
            return new CurrencyConversionResult(amount, from, to, BigDecimal.ONE, amount);
        }

        Map<CurrencyUnit, Double> rates = api.getLatestRates(baseCurrency);

        if (!rates.containsKey(from) || !rates.containsKey(to)) {
            throw new IllegalArgumentException("Tasso di cambio non disponibile per le valute selezionate.");
        }

        BigDecimal fromRate = BigDecimal.valueOf(rates.get(from));
        BigDecimal toRate = BigDecimal.valueOf(rates.get(to));

        BigDecimal amountInBase = amount.divide(fromRate, 20, RoundingMode.HALF_UP);
        BigDecimal converted = amountInBase.multiply(toRate)
            .setScale(10, RoundingMode.HALF_UP);

        BigDecimal appliedRate = toRate.divide(fromRate, 10, RoundingMode.HALF_UP);

        return new CurrencyConversionResult(amount, from, to, appliedRate, converted);
    }
}
