package it.unibo.unibodget.model.currency.engin;

import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Standard implementation of {@link CurrencyConverter} that performs conversions
 * using exchange rates retrieved from an {@link ExchangeRateAPI}. All conversions
 * are computed relative to a fixed internal base currency, which simplifies
 * rate management and reduces the number of required API calls.
 */
public class BasicCurrencyConverter implements CurrencyConverter {

    private static final int RATE_SCALE = 20;
    private static final int RESULT_SCALE = 10;

    private final UniversalPriceService priceService;

    /**
     * Creates a new {@code BasicCurrencyConverter}.
     * Excludes STOCK-type currencies from conversion capabilities,
     * and relies on the provided API to obtain exchange rates for supported currencies
     * checking taxes existence
     *
     * @param api the exchange-rate provider used to obtain conversion data
     * @param baseCurrency the internal base currency used for intermediate conversions
     */
    public BasicCurrencyConverter(final UniversalPriceService priceService) {
        this.priceService = Objects.requireNonNull(priceService);
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
    public CurrencyConversionResult convert(
            final BigDecimal amount,
            final CurrencyUnit from,
            final CurrencyUnit to) {

        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");

        System.out.println("[CONVERSION] Request: "
                + amount + " " + from.getCode()
                + " (" + from.getType() + ") -> "
                + to.getCode() + " (" + to.getType() + ")");

        if (from.getCode().equalsIgnoreCase(to.getCode())) {
            System.out.println("[CONVERSION] Same asset: rate = 1, result = "
                    + amount);

            return new CurrencyConversionResult(
                    amount, from, to, BigDecimal.ONE, amount
            );
        }

        final BigDecimal fromUsd = priceService.getPriceInUSD(from);
        System.out.println("[CONVERSION] 1 " + from.getCode()
                + " = " + fromUsd + " USD");

        final BigDecimal toUsd = priceService.getPriceInUSD(to);
        System.out.println("[CONVERSION] 1 " + to.getCode()
                + " = " + toUsd + " USD");

        if (fromUsd == null || fromUsd.signum() <= 0
                || toUsd == null || toUsd.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Prezzo non valido per " + from.getCode()
                            + " o " + to.getCode()
            );
        }

        final BigDecimal appliedRate = fromUsd.divide(
                toUsd,
                RATE_SCALE,
                RoundingMode.HALF_UP
        );

        final BigDecimal converted = amount.multiply(appliedRate)
                .setScale(RESULT_SCALE, RoundingMode.HALF_UP);
        return new CurrencyConversionResult(
                amount, from, to, appliedRate, converted
        );
    }

}
