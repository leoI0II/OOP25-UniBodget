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
     * Converts {@code src} into the equivalent amount expressed in {@code target}.
     *
     * @param src    the asset to convert, carrying both the amount and the source currency
     * @param target the currency to convert into
     * @return a new {@link Asset} with the converted amount in {@code target}
     */
    Asset convert(Asset src, CurrencyUnit target);

}
