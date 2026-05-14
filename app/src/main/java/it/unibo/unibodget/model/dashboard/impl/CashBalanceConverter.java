package it.unibo.unibodget.model.dashboard.impl;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Converts a cash balance into a target currency.
 *
 * <p>This abstraction allows the cash dashboard model to depend on a currency
 * conversion contract without knowing the concrete implementation provided
 * by the currency-converter subsystem.</p>
 */
public interface CashBalanceConverter {

    /**
     * Converts the given asset into the target currency.
     *
     * @param asset
     *            the asset to convert
     * @param targetCurrency
     *            the desired currency
     * @return the converted asset expressed in the target currency
     */
    Asset convert(Asset asset, CurrencyUnit targetCurrency);
}