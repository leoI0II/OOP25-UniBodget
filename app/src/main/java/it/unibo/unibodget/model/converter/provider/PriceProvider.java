package it.unibo.unibodget.model.converter.provider;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Defines a service capable of retrieving the current market price of an asset
 * expressed in a specified target currency.
 *
 * <p>
 * Implementations of this interface provide the logic required to obtain
 * up‑to‑date pricing information for assets such as fiat currencies, stocks,
 * or cryptocurrencies. The price may be obtained from external APIs, local
 * caches, or predefined mock values depending on the specific provider.
 * </p>
 */
public interface PriceProvider {

    /**
     * Gets the current price of the given asset in the target currency.
     * 
     * @param asset the asset for which to get the price (ex. USD, AAPL, BTC)
     * @param targetCurrency the currency in which to express the price
     * @return the current price of the asset in the target currency
     */
    Asset getCurrentPrice(CurrencyUnit asset, CurrencyUnit targetCurrency);

}
