package it.unibo.unibodget.app;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.dashboard.impl.CashBalanceConverter;

final class InMemoryCashBalanceConverter implements CashBalanceConverter {
    @Override
    public Asset convert(final Asset asset, final CurrencyUnit targetCurrency) {
        return Asset.of(targetCurrency, asset.amount());
    }
}
