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

    private static final String VAL_1 = "1";
    private static final String VAL_2 = "2";
    private static final String VAL_3 = "3";
    private static final String VAL_5 = "5";
    private static final String VAL_10 = "10";
    private static final String INVEST = "Invest";
    private static final String QTY_3 = "3";
    private static final String QTY_5 = "5";
    private static final String QTY_10 = "10";

    // PriceProvider fake: current price = 2 EUR
    private final PriceProvider provider = new PriceProvider() {
        @Override
        public Asset getCurrentPrice(final CurrencyUnit asset, final CurrencyUnit base) {
            return new Asset(base, new BigDecimal(VAL_2));
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
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1));
        acc.addTransaction(buy(FiatCurrency.USD, QTY_5, VAL_2));
        acc.addTransaction(sell(FiatCurrency.USD, QTY_3, VAL_3));

        final List<Position> positions = acc.getPositions();
        assertEquals(1, positions.size());

        final Position p = positions.get(0);

        // 10 + 5 - 3 = 12
        assertEquals(new BigDecimal("12"), p.quantity());
    }

    @Test
    void shouldComputeBalanceFromMarketValue() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1));

        // prezzo corrente = 2 → valore = 10 * 2 = 20
        assertEquals(0,
            acc.getBalance().amount().compareTo(new BigDecimal("20"))
        );
    }

    @Test
    void shouldComputeUnrealizedProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1));

        // cost = 10, market = 20 → P/L = 10
        assertEquals(0,
            acc.getUnrealizedProfitLoss().amount().compareTo(new BigDecimal(VAL_10))
        );
    }

    @Test
    void shouldComputeRealizedProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1)); // cost = 10
        acc.addTransaction(sell(FiatCurrency.USD, QTY_5, VAL_3)); // realized = (3 - 1) * 5 = 10

        assertEquals(0,
            acc.getRealizedProfitLoss().amount().compareTo(new BigDecimal(VAL_10))
        );
    }

    @Test
    void shouldComputeTotalProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1)); // unrealized = 10
        acc.addTransaction(sell(FiatCurrency.USD, QTY_5, VAL_3)); // realized = 10

        // unrealized dopo la vendita = 5 * (2 - 1) = 5
        // realized = 10
        // totale = 15
        assertEquals(0,
            acc.getTotalProfitLoss().amount().compareTo(new BigDecimal("15"))
        );
    }

    @Test
    void shouldCheckCanSell() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1));

        assertTrue(acc.canSell(FiatCurrency.USD, new BigDecimal(VAL_5)));
        assertFalse(acc.canSell(FiatCurrency.USD, new BigDecimal("20")));
    }

    @Test
    void shouldRejectInvalidSellQuantity() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        assertThrows(IllegalArgumentException.class, () ->
                acc.canSell(FiatCurrency.USD, new BigDecimal("-1"))
        );
    }

}
