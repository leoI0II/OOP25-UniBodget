package it.unibo.unibodget.model.converter.provider;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;

public interface ExchangeRateProvider {
    
    Asset convert(Asset src, CurrencyUnit target);
    
}
