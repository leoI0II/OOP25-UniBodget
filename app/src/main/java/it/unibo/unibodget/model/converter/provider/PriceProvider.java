package it.unibo.unibodget.model.converter.provider;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;

public interface PriceProvider {
    
    /**
     * Gets the current price of the given asset in the target currency.
     * 
     * @param asset the asset for which to get the price (ex. USD, AAPL, BTC)
     * @param targetCurrency the currency in which to express the price
     * @return the current price of the asset in the target currency
     */
    public Asset getCurrentPrice(final CurrencyUnit asset, final CurrencyUnit targetCurrency);
    
}
