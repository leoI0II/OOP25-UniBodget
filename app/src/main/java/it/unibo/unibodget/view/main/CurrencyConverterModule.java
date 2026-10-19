package it.unibo.unibodget.view.main;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.view.currency_converter.CurrencyConverterViewFX;
import java.util.Objects;
import javafx.scene.Parent;

/**
 * Wires the currency-converter dashboard (Arianna's module) so it can be embedded as a content-only
 * panel inside the shared FXML shell.
 *
 * <p>The heavy view is built lazily on the first {@link #getView()} call, so the converter widgets
 * are created only when the user first opens that context.
 */
public final class CurrencyConverterModule {

    private final CurrencyConverterController controller;
    private Parent view;

    /**
     * @param api the exchange-rate provider used for the latest rate table
     * @param converter the shared conversion engine (must be a {@link BasicCurrencyConverter}, as
     *     required by the bank widget)
     */
    public CurrencyConverterModule(
            final ExchangeRateAPI api, final BasicCurrencyConverter converter) {
        Objects.requireNonNull(api);
        Objects.requireNonNull(converter);
        this.controller = new CurrencyConverterController(api, converter);
    }

    /**
     * @return the converter dashboard node, built on first access and cached afterwards
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Module exposes cached view for UI embedding")
    public Parent getView() {
        if (view == null) {
            view = CurrencyConverterViewFX.buildContent(controller);
        }
        return view;
    }
}
