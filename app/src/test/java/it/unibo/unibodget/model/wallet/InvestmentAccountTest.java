package it.unibo.unibodget.model.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link InvestmentAccount}.
 *
 * <p>Verifies investment positions, balances, order validation,
 * and profit calculations under both synthetic and fixed price models.
 */
class InvestmentAccountTest {

    private static final String VAL_1 = "1";
    private static final String VAL_2 = "2";
    private static final String VAL_3 = "3";
    private static final String VAL_5 = "5";
    private static final String INVEST = "Invest";
    private static final String QTY_3 = "3";
    private static final String QTY_5 = "5";
    private static final String QTY_10 = "10";

    private static final int RATE_SCALE = 20;
    private static final int TWO_POSITIONS = 2;

    private static final BigDecimal TWO_UNITS = BigDecimal.valueOf(2);
    private static final BigDecimal FIVE_UNITS = BigDecimal.valueOf(5);

    private static final BigDecimal BTC_FIRST_PRICE = new BigDecimal("50000");
    private static final BigDecimal BTC_SECOND_PRICE = new BigDecimal("55000");
    private static final BigDecimal BTC_AVERAGE_PRICE = new BigDecimal("52500");
    private static final BigDecimal ETH_PRICE = new BigDecimal("4000");

    private static final BigDecimal AAPL_LOW_PRICE = new BigDecimal("100");
    private static final BigDecimal AAPL_MARKET_PRICE = new BigDecimal("150");
    private static final BigDecimal AAPL_HIGH_PRICE = new BigDecimal("200");
    private static final BigDecimal AAPL_LOSS_SALE_PRICE = new BigDecimal("80");

    private static final BigDecimal EXPECTED_GAIN = new BigDecimal("500");
    private static final BigDecimal EXPECTED_FIRST_LOSS = new BigDecimal("-100");
    private static final BigDecimal EXPECTED_FINAL_GAIN = new BigDecimal("700");

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 1, 1);

    private static final Map<String, BigDecimal> USD_PRICES = Map.of(
            FiatCurrency.USD.getCode(), BigDecimal.ONE,
            CryptoCurrency.BTC.getCode(), BTC_FIRST_PRICE,
            CryptoCurrency.ETH.getCode(), ETH_PRICE,
            StockMarketCurrency.AAPL.getCode(), AAPL_MARKET_PRICE
    );

    // CurrencyConverter fake: current price = 2 EUR
    private final CurrencyConverter provider =
            (amount, from, to) -> {
                final BigDecimal rate;

                if (from.getCode().equalsIgnoreCase(to.getCode())) {
                    rate = BigDecimal.ONE;
                } else if ("USD".equalsIgnoreCase(from.getCode())
                        && "EUR".equalsIgnoreCase(to.getCode())) {
                    rate = BigDecimal.valueOf(2);
                } else {
                    throw new IllegalArgumentException(
                            "Unsupported test conversion: "
                                    + from.getCode()
                                    + " -> "
                                    + to.getCode());
                }

                return new CurrencyConversionResult(amount, from, to, rate, amount.multiply(rate));
            };

    private InvestmentAccount account;

    /**
     * Creates an independent account before each test for fixed-price suite.
     */
    @BeforeEach
    void setUp() {
        final CurrencyConverter converter =
                InvestmentAccountTest::convertAtFixedPrice;

        account = new InvestmentAccount(
                "Test Account",
                FiatCurrency.USD,
                converter
        );
    }

    private InvestmentTransaction buy(
            final CurrencyUnit asset, final String qty, final String price) {
        return new InvestmentTransaction(
                new Asset(asset, new BigDecimal(qty)),
                Category.INVESTMENT_BUY,
                LocalDate.now(),
                "buy",
                "",
                new Asset(asset, new BigDecimal(price)),
                null);
    }

    /**
     * Adds a purchase with no fees.
     *
     * @param currency the purchased asset
     * @param quantity the positive purchased quantity
     * @param unitPrice the purchase price in USD
     */
    private void buy(
            final CurrencyUnit currency,
            final BigDecimal quantity,
            final BigDecimal unitPrice) {

        addTransaction(currency, quantity, unitPrice, Category.INVESTMENT_BUY);
    }

    private InvestmentTransaction sell(
            final CurrencyUnit asset, final String qty, final String price) {
        return new InvestmentTransaction(
                new Asset(asset, new BigDecimal(qty).negate()),
                Category.INVESTMENT_BUY,
                LocalDate.now(),
                "sell",
                "",
                new Asset(asset, new BigDecimal(price)),
                null);
    }

    /**
     * Adds a sale with no fees.
     *
     * @param currency the sold asset
     * @param quantity the positive sold quantity
     * @param unitPrice the sale price in USD
     */
    private void sell(
            final CurrencyUnit currency,
            final BigDecimal quantity,
            final BigDecimal unitPrice) {

        addTransaction(
                currency,
                quantity.negate(),
                unitPrice,
                Category.INVESTMENT_SELL
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
        assertEquals(0, new BigDecimal("12").compareTo(p.quantity()));
    }

    @Test
    void shouldComputeBalanceFromMarketValue() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1));

        // prezzo corrente = 2 → valore = 10 * 2 = 20
        assertEquals(0, acc.getBalance().amount().compareTo(new BigDecimal("20")));
    }

    @Test
    void shouldComputeUnrealizedProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1));

        // cost = 10, market = 20 → P/L = 10
        assertEquals(0, acc.getUnrealizedProfitLoss().amount().compareTo(BigDecimal.TEN));
    }

    @Test
    void shouldComputeRealizedProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1)); // cost = 10
        acc.addTransaction(sell(FiatCurrency.USD, QTY_5, VAL_3)); // realized = (3 - 1) * 5 = 10

        assertEquals(0, acc.getRealizedProfitLoss().amount().compareTo(BigDecimal.TEN));
    }

    @Test
    void shouldComputeTotalProfitLoss() {
        final InvestmentAccount acc = new InvestmentAccount(INVEST, FiatCurrency.EUR, provider);

        acc.addTransaction(buy(FiatCurrency.USD, QTY_10, VAL_1)); // unrealized = 10
        acc.addTransaction(sell(FiatCurrency.USD, QTY_5, VAL_3)); // realized = 10

        // unrealized dopo la vendita = 5 * (2 - 1) = 5
        // realized = 10
        // totale = 15
        assertEquals(0, acc.getTotalProfitLoss().amount().compareTo(new BigDecimal("15")));
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

        assertThrows(
                IllegalArgumentException.class,
                () -> acc.canSell(FiatCurrency.USD, new BigDecimal("-1")));
    }

    @Test
    void testEmptyPositionsList() {
        assertTrue(account.getPositions().isEmpty());
    }

    @Test
    void testSingleBuy() {
        buy(CryptoCurrency.BTC, BigDecimal.ONE, BTC_FIRST_PRICE);

        assertEquals(1, account.getPositions().size());

        final var position = account.getPositionForAsset(CryptoCurrency.BTC)
                .orElseThrow();

        assertAmountEquals(BigDecimal.ONE, position.quantity());
        assertAmountEquals(BTC_FIRST_PRICE, position.averageBasisCost().amount());
    }

    @Test
    void testTwoBuysOfOneAsset() {
        buy(CryptoCurrency.BTC, BigDecimal.ONE, BTC_FIRST_PRICE);
        buy(CryptoCurrency.BTC, BigDecimal.ONE, BTC_SECOND_PRICE);

        assertEquals(1, account.getPositions().size());

        final var position = account.getPositionForAsset(CryptoCurrency.BTC)
                .orElseThrow();

        assertAmountEquals(TWO_UNITS, position.quantity());
        assertAmountEquals(BTC_AVERAGE_PRICE, position.averageBasisCost().amount());
    }

    @Test
    void testBuyNSellPosition() {
        buy(CryptoCurrency.BTC, BigDecimal.ONE, BTC_SECOND_PRICE);
        sell(CryptoCurrency.BTC, BigDecimal.ONE, BTC_SECOND_PRICE);

        assertTrue(account.getPositions().isEmpty());
    }

    @Test
    void testTwoDiffBuys() {
        buy(CryptoCurrency.BTC, BigDecimal.ONE, BTC_SECOND_PRICE);
        buy(CryptoCurrency.ETH, TWO_UNITS, ETH_PRICE);

        assertEquals(TWO_POSITIONS, account.getPositions().size());

        final var btcPosition = account.getPositionForAsset(CryptoCurrency.BTC)
                .orElseThrow();
        final var ethPosition = account.getPositionForAsset(CryptoCurrency.ETH)
                .orElseThrow();

        assertAmountEquals(BigDecimal.ONE, btcPosition.quantity());
        assertAmountEquals(TWO_UNITS, ethPosition.quantity());

        assertEquals(CryptoCurrency.BTC.getCode(), btcPosition.asset().getCode());
        assertEquals(CryptoCurrency.ETH.getCode(), ethPosition.asset().getCode());
    }

    @Test
    void testEmptyWalletZeroBalance() {
        assertAmountEquals(BigDecimal.ZERO, account.getBalance().amount());
    }

    @Test
    void testBalanceAfterSingleBuy() {
        buy(StockMarketCurrency.AAPL, BigDecimal.TEN, AAPL_MARKET_PRICE);

        assertAmountEquals(
                AAPL_MARKET_PRICE.multiply(BigDecimal.TEN),
                account.getBalance().amount()
        );
    }

    @Test
    void testSingleBuyProfit() {
        buy(StockMarketCurrency.AAPL, BigDecimal.TEN, AAPL_LOW_PRICE);

        assertAmountEquals(
                EXPECTED_GAIN,
                account.getUnrealizedProfitLoss().amount()
        );

        assertAmountEquals(
                EXPECTED_GAIN,
                account.getPositionForAsset(StockMarketCurrency.AAPL)
                        .orElseThrow()
                        .getUnrealizedProfitLoss()
                        .amount()
        );
    }

    @Test
    void testSingleBuyLoss() {
        buy(StockMarketCurrency.AAPL, BigDecimal.TEN, AAPL_HIGH_PRICE);

        assertAmountEquals(
                EXPECTED_GAIN.negate(),
                account.getUnrealizedProfitLoss().amount()
        );

        assertAmountEquals(
                EXPECTED_GAIN.negate(),
                account.getPositionForAsset(StockMarketCurrency.AAPL)
                        .orElseThrow()
                        .getUnrealizedProfitLoss()
                        .amount()
        );
    }

    @Test
    void testRealizedPLZero() {
        buy(CryptoCurrency.BTC, BigDecimal.ONE, BTC_FIRST_PRICE);

        assertAmountEquals(
                BigDecimal.ZERO,
                account.getRealizedProfitLoss().amount()
        );
    }

    @Test
    void testRealizedPLOneBuyOneSell() {
        buy(StockMarketCurrency.AAPL, BigDecimal.TEN, AAPL_LOW_PRICE);
        sell(StockMarketCurrency.AAPL, FIVE_UNITS, AAPL_HIGH_PRICE);

        assertAmountEquals(
                EXPECTED_GAIN,
                account.getRealizedProfitLoss().amount()
        );
    }

    @Test
    void testRealizedPLMultipleBuysSells() {
        buy(StockMarketCurrency.AAPL, BigDecimal.TEN, AAPL_LOW_PRICE);
        sell(StockMarketCurrency.AAPL, FIVE_UNITS, AAPL_LOSS_SALE_PRICE);

        assertAmountEquals(
                EXPECTED_FIRST_LOSS,
                account.getRealizedProfitLoss().amount()
        );

        buy(StockMarketCurrency.AAPL, BigDecimal.TEN, AAPL_LOW_PRICE);
        buy(StockMarketCurrency.AAPL, BigDecimal.TEN, AAPL_MARKET_PRICE);
        sell(StockMarketCurrency.AAPL, BigDecimal.TEN, AAPL_HIGH_PRICE);

        assertAmountEquals(
                EXPECTED_FINAL_GAIN,
                account.getRealizedProfitLoss().amount()
        );
    }

    /**
     * Adds an investment transaction.
     *
     * @param currency the traded asset
     * @param signedQuantity the signed traded quantity
     * @param unitPrice the unit price in USD
     * @param category the transaction category
     */
    private void addTransaction(
            final CurrencyUnit currency,
            final BigDecimal signedQuantity,
            final BigDecimal unitPrice,
            final Category category) {

        account.addTransaction(InvestmentTransaction.of(
                Asset.of(currency, signedQuantity),
                category,
                TEST_DATE,
                "",
                "",
                Asset.of(FiatCurrency.USD, unitPrice),
                Asset.zero(FiatCurrency.USD)
        ));
    }

    /**
     * Converts an amount using fixed USD prices.
     *
     * @param amount the amount to convert
     * @param from the source currency
     * @param to the target currency
     * @return the conversion result
     */
    private static CurrencyConversionResult convertAtFixedPrice(
            final BigDecimal amount,
            final CurrencyUnit from,
            final CurrencyUnit to) {

        final BigDecimal rate;

        if (from.getCode().equals(to.getCode())) {
            rate = BigDecimal.ONE;
        } else {
            rate = getUsdPrice(from).divide(
                    getUsdPrice(to),
                    RATE_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        return new CurrencyConversionResult(
                amount,
                from,
                to,
                rate,
                amount.multiply(rate)
        );
    }

    /**
     * Returns the fixed USD price configured for an asset.
     *
     * @param currency the currency to price
     * @return the configured USD price
     */
    private static BigDecimal getUsdPrice(final CurrencyUnit currency) {
        final BigDecimal price = USD_PRICES.get(currency.getCode());

        if (price == null) {
            throw new IllegalArgumentException(
                    "No test price configured for " + currency.getCode()
            );
        }

        return price;
    }

    /**
     * Compares amounts independently of their decimal scale.
     *
     * @param expected the expected amount
     * @param actual the actual amount
     */
    private static void assertAmountEquals(
            final BigDecimal expected,
            final BigDecimal actual) {

        assertEquals(
                expected.stripTrailingZeros(),
                actual.stripTrailingZeros()
        );
    }
}
