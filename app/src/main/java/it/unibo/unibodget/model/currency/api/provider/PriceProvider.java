package it.unibo.unibodget.model.currency.api.provider;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Returns the USD value of one unit of each given asset.
     *
     * <p>The default asks the price of each asset one by one (one request each).
     * Providers whose API accepts more assets in the same request (e.g. CoinGecko)
     * should override it, so all the prices cost only one request.</p>
     *
     * @param units the assets
     * @return the USD price of one unit for each asset; an asset can be missing
     *         if its price is not available
     */
    default Map<CurrencyUnit, BigDecimal> getPricesInUSD(final List<? extends CurrencyUnit> units) {
        final Map<CurrencyUnit, BigDecimal> prices = new HashMap<>();
        for (final CurrencyUnit unit : units) {
            prices.put(unit, getPriceInUSD(unit));
        }
        return prices;
    }
}
