package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Logical view for the currency converter feature. This class represents the
 * UI-facing layer and delegates all operations to the {@link CurrencyConverterController}
 */
public class CurrencyConverterView {

    private static final String NEWLINE = "\n";
    private final CurrencyConverterController controller;

    /**
     * Creates a new {@code CurrencyConverterView}.
     *
     * @param controller the controller responsible for handling currency operations
     */
    public CurrencyConverterView(final CurrencyConverterController controller) {
        this.controller = controller;
    }

    /**
     * Performs a currency conversion and returns a formatted string suitable for display.
     *
     * @param amount the amount to convert
     * @param from   the source currency
     * @param to     the target currency
     * @return a human-readable string describing the conversion result
     */
    public String convert(final double amount, final CurrencyUnit from, final CurrencyUnit to) {
        final BigDecimal result = controller.convert(BigDecimal.valueOf(amount), from, to);

        System.out.println("Conversion result: " + result
                            + " (amount: " + amount + ", from: " 
                            + from.getCode() + ", to: " + to.getCode() + ")");

        return amount + " " + from.getCode() + " = " + result + " " + to.getCode();
    }

    /**
     * Retrieves historical exchange-rate data and returns it in a display-friendly format.
     *
     * @param base   the base currency
     * @param target the target currency
     * @param from   the start date
     * @param to     the end date
     * @return a formatted string containing the historical rate series
     */
    public String getHistoricalRates(final CurrencyUnit base, final CurrencyUnit target,
                                    final LocalDate from, final LocalDate to) {

        final Map<LocalDate, Double> history =
                controller.getHistoricalRates(base, target, from, to);

        final StringBuilder sb = new StringBuilder();
        sb.append("Historical rates ").append(base.getCode())
          .append(" → ").append(target.getCode()).append(NEWLINE);

        history.forEach((date, rate) ->
                sb.append(date).append(": ").append(rate).append(NEWLINE));

        return sb.toString();
    }

    /**
     * Retrieves the latest exchange-rate table for display.
     *
     * @param base the base currency
     * @return a formatted string containing the latest rate table
     */
    public String getLatestRates(final CurrencyUnit base) {
        final Map<CurrencyUnit, Double> rates = controller.getLatestRates(base);

        final StringBuilder sb = new StringBuilder();
        sb.append("Latest rates (base ").append(base.getCode()).append(NEWLINE);

        rates.forEach((unit, rate) ->
                sb.append(unit.getCode()).append(": ").append(rate).append(NEWLINE));

        return sb.toString();
    }
}
