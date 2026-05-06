package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency.CurrencyConverterController;
import it.unibo.unibodget.model.currency.CurrencyUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Logical view for the currency converter feature. This class represents the
 * UI-facing layer and delegates all operations to the {@link CurrencyConverterController}.
 *
 * <p>This implementation is framework-agnostic: it does not depend on Swing,
 * JavaFX, or any graphical toolkit. Concrete UI components should wrap this
 * class and use its methods to retrieve data and perform conversions.</p>
 */
public class CurrencyConverterView {

    private final CurrencyConverterController controller;

    /**
     * Creates a new {@code CurrencyConverterView}.
     *
     * @param controller the controller responsible for handling currency operations
     */
    public CurrencyConverterView(CurrencyConverterController controller) {
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
    public String convert(double amount, CurrencyUnit from, CurrencyUnit to) {
        BigDecimal result = controller.convert(BigDecimal.valueOf(amount), from, to);

        System.out.println("Conversion result: " + result + 
                            " (amount: " + amount + ", from: " 
                            + from.getCode() + ", to: " + to.getCode() + ")");

        return amount + " " + from.getCode() + " = "
                + result + " " + to.getCode();
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
    public String getHistoricalRates(CurrencyUnit base, CurrencyUnit target,
                                     LocalDate from, LocalDate to) {

        Map<LocalDate, Double> history =
                controller.getHistoricalRates(base, target, from, to);

        StringBuilder sb = new StringBuilder();
        sb.append("Historical rates ").append(base.getCode())
          .append(" → ").append(target.getCode()).append("\n");

        history.forEach((date, rate) ->
                sb.append(date).append(": ").append(rate).append("\n"));

        return sb.toString();
    }

    /**
     * Retrieves the latest exchange-rate table for display.
     *
     * @param base the base currency
     * @return a formatted string containing the latest rate table
     */
    public String getLatestRates(CurrencyUnit base) {
        Map<CurrencyUnit, Double> rates = controller.getLatestRates(base);

        StringBuilder sb = new StringBuilder();
        sb.append("Latest rates (base ").append(base.getCode()).append(")\n");

        rates.forEach((unit, rate) ->
                sb.append(unit.getCode()).append(": ").append(rate).append("\n"));

        return sb.toString();
    }
}
