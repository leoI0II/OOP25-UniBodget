package it.unibo.unibodget.model.converter.provider;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;

/**
 * Strategy for converting an {@link Asset} from one currency to another.
 *
 * <p>Implementations may fetch live rates from an external API, use a static
 * lookup table, or apply any other exchange-rate strategy.</p>
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
