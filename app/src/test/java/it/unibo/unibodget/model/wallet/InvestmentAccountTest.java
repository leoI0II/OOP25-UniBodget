package it.unibo.unibodget.model.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.converter.provider.PriceProvider;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

class InvestmentAccountTest {

    // PriceProvider finto: prezzo corrente = 2 EUR
    final PriceProvider provider = new PriceProvider() {
        @Override
        public Asset getCurrentPrice(final CurrencyUnit asset, final CurrencyUnit base) {
            return new Asset(base, new BigDecimal("2"));
        }
    };

    private InvestmentTransaction buy(final CurrencyUnit asset, final String qty, final String price) {
        return new InvestmentTransaction(
                new Asset(asset, new BigDecimal(qty)),
                Category.INVESTMENT_BUY,
                LocalDate.now(),
                "buy",
                "",
                new Asset(asset, new BigDecimal(price)),
                null
        );
    }

    private InvestmentTransaction sell(final CurrencyUnit asset, final String qty, final String price) {
        return new InvestmentTransaction(
                new Asset(asset, new BigDecimal(qty).negate()),
                Category.INVESTMENT_BUY,
                LocalDate.now(),
                "sell",
                "",
                new Asset(asset, new BigDecimal(price)),
                null
        );
    }

    @Test
    void shouldComputePositionsCorrectly() {
        final InvestmentAccount acc = new InvestmentAccount("Invest", FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, "10", "1"));
        acc.addTransaction(buy(FiatCurrency.USD, "5", "2"));
        acc.addTransaction(sell(FiatCurrency.USD, "3", "3"));

        final List<Position> positions = acc.getPositions();
        assertEquals(1, positions.size());

        final Position p = positions.get(0);

        // 10 + 5 - 3 = 12
        assertEquals(new BigDecimal("12"), p.quantity());
    }

    @Test
    void shouldComputeBalanceFromMarketValue() {
        final InvestmentAccount acc = new InvestmentAccount("Invest", FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, "10", "1"));

        // prezzo corrente = 2 → valore = 10 * 2 = 20
        assertEquals(0,
            acc.getBalance().amount().compareTo(new BigDecimal("20"))
        );
    }

    @Test
    void shouldComputeUnrealizedProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount("Invest", FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, "10", "1"));

        // cost = 10, market = 20 → P/L = 10
        assertEquals(0,
            acc.getUnrealizedProfitLoss().amount().compareTo(new BigDecimal("10"))
        );
    }

    @Test
    void shouldComputeRealizedProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount("Invest", FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, "10", "1")); // cost = 10
        acc.addTransaction(sell(FiatCurrency.USD, "5", "3")); // realized = (3 - 1) * 5 = 10

        assertEquals(0,
            acc.getRealizedProfitLoss().amount().compareTo(new BigDecimal("10"))
        );
    }

    @Test
    void shouldComputeTotalProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount("Invest", FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, "10", "1")); // unrealized = 10
        acc.addTransaction(sell(FiatCurrency.USD, "5", "3")); // realized = 10

        // unrealized dopo la vendita = 5 * (2 - 1) = 5
        // realized = 10
        // totale = 15
        assertEquals(0,
            acc.getTotalProfitLoss().amount().compareTo(new BigDecimal("15"))
        );
    }

    @Test
    void shouldCheckCanSell() {
        final InvestmentAccount acc = new InvestmentAccount("Invest", FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, "10", "1"));

        assertTrue(acc.canSell(FiatCurrency.USD, new BigDecimal("5")));
        assertFalse(acc.canSell(FiatCurrency.USD, new BigDecimal("20")));
    }

    @Test
    void shouldRejectInvalidSellQuantity() {
        final InvestmentAccount acc = new InvestmentAccount("Invest", FiatCurrency.EUR, provider);

        assertThrows(IllegalArgumentException.class, () ->
                acc.canSell(FiatCurrency.USD, new BigDecimal("-1"))
        );
    }

}
