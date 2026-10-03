package it.unibo.unibodget.model.currency.api.provider;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Defines a provider capable of retrieving the current market value
 * of a currency or asset expressed in United States Dollars (USD).
 *
 * <p>
 * Implementations may rely on different external services depending
 * on the asset type:
 *
 * <ul>
 *     <li>CoinGecko for cryptocurrencies</li>
 *     <li>Alpha Vantage for stocks</li>
 *     <li>Other providers for additional asset classes</li>
 * </ul>
 */
public interface PriceProvider {

    /**
     * Returns the current value of a single asset unit expressed in USD.
     *
     * @param unit the asset
     * @return the USD value of one unit
     */
    BigDecimal getPriceInUSD(CurrencyUnit unit);
}
