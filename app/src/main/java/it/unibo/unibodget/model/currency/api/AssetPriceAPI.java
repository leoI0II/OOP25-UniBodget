package it.unibo.unibodget.model.currency.api;

import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Defines an API for retrieving asset prices expressed in USD.
 */
public interface AssetPriceAPI {

    /**
     * Returns the price of one unit of the specified asset in USD.
     *
     * @param asset the asset whose price is requested
     * @return the USD price of one asset unit
     */
    double getPriceInUSD(CurrencyUnit asset);
}
