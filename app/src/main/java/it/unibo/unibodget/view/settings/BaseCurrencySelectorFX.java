package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import javafx.scene.control.ComboBox;

/**
 * Factory class for creating a ComboBox used to select the application's
 * base currency in the Settings view.
 *
 * <p>The selector is populated with all currencies currently loaded
 * by the system via {@link Currency#all()}.</p>
 */
public final class BaseCurrencySelectorFX {

    /**
     * Creates a ComboBox containing all available {@link CurrencyUnit} values.
     *
     * @return a ComboBox populated with all currencies
     */
    public static ComboBox<CurrencyUnit> createCurrencySelector() {
        ComboBox<CurrencyUnit> box = new ComboBox<>();
        box.getItems().addAll(Currency.all());
        return box;
    }
}
