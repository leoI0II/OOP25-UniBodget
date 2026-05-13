package it.unibo.unibodget.model.converter.provider;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.StockMarketCurrency;

public class MockPriceProvider implements PriceProvider {

    @Override
    public Asset getCurrentPrice(CurrencyUnit asset, CurrencyUnit targetCurrency) {
        if (asset.equals(targetCurrency)) {
            return Asset.of(targetCurrency, new BigDecimal("1.0")); // 1:1 for same type (e.g., USD to USD)
        }
        else if (asset.equals(FiatCurrency.EUR) && targetCurrency.equals(FiatCurrency.USD)) {
            return Asset.of(targetCurrency, new BigDecimal("1.1")); // Mock EUR to USD rate
        }
        else if (asset.equals(FiatCurrency.USD) && targetCurrency.equals(FiatCurrency.EUR)) {
            return Asset.of(targetCurrency, new BigDecimal("0.9")); // Mock USD to EUR rate
        }
        else if (asset.equals(CryptoCurrency.BTC) && 
            (targetCurrency.equals(CryptoCurrency.USDT) || targetCurrency.equals(FiatCurrency.USD))) {
            return Asset.of(targetCurrency, new BigDecimal("50000.0")); // Mock BTC to USDT rate
        }
        else if (asset.equals(CryptoCurrency.ETH) && 
            (targetCurrency.equals(CryptoCurrency.USDT) || targetCurrency.equals(FiatCurrency.USD))) {
            return Asset.of(targetCurrency, new BigDecimal("4000.0")); // Mock ETH to USDT rate
        }
        else if (asset.equals(StockMarketCurrency.AAPL) && targetCurrency.equals(FiatCurrency.USD)) {
            return Asset.of(targetCurrency, new BigDecimal("150.0")); // Mock AAPL to USD price
        }
        else {
            return Asset.of(targetCurrency, new BigDecimal("100.0")); // Default mock price for other assets
        }
    }
    
}
