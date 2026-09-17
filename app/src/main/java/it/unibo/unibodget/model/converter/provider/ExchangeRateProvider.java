package it.unibo.unibodget.model.converter.provider;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Defines a strategy for converting an {@link Asset} from one currency to another.
 *
 * <p>
 * Implementations of this interface provide the logic required to obtain or compute
 * exchange rates between currencies. The conversion may rely on external APIs,
 * cached data, or predefined mock values depending on the specific provider.
 * </p>
 */
public interface ExchangeRateProvider {

    /**
     * Converts the given asset into the specified target currency.
     *
     * <p>
     * The returned {@link Asset} represents the same economic value expressed
     * in the target currency.
     * </p>
     *
     * @param src    the asset to convert; must not be {@code null}
     * @param target the currency into which the asset should be converted;
     *               must not be {@code null}
     * @return a new {@link Asset} expressed in the target currency
     */
    Asset convert(Asset src, CurrencyUnit target);


}
