package it.unibo.unibodget.investment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.controllers.DefaultInvestmentController;
import it.unibo.unibodget.model.investment.service.CSVInvestmentsSnapshotService;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests investment account management and order execution using fixed rates.
 *
 * <p>No external price services or network connections are used.
 */
final class DefaultInvestmentControllerTest {

    private static final int RATE_SCALE = 20;
    private static final int RESULT_SCALE = 10;

    private static final BigDecimal AAPL_PRICE = new BigDecimal("150");
    private static final BigDecimal BTC_PRICE = new BigDecimal("50000");
    private static final BigDecimal ETH_PRICE = new BigDecimal("4000");
    private static final BigDecimal USD_TO_EUR = new BigDecimal("0.91");
    private static final BigDecimal INITIAL_CASH = new BigDecimal("10000");
    private static final BigDecimal ORDER_FEE = new BigDecimal("5");
    private static final BigDecimal TWO_UNITS = BigDecimal.valueOf(2);
    private static final BigDecimal FIVE_UNITS = new BigDecimal("5");
    private static final BigDecimal NINE_UNITS = new BigDecimal("9");
    private static final BigDecimal LARGE_QUANTITY = new BigDecimal("100");
    private static final BigDecimal HALF_UNIT = new BigDecimal("0.5");
    private static final BigDecimal STABLECOIN_QUANTITY = new BigDecimal("1000");
    private static final BigDecimal PURCHASE_BASIS_PRICE = new BigDecimal("100");

    private static final String OTHER_ACCOUNT_NAME = "Other";
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 1, 1);

    /*
     * Prices expressed in EUR. Using EUR as the reference preserves the
     * exact USD-to-EUR rate without first approximating its reciprocal.
     */
    private static final Map<String, BigDecimal> REFERENCE_PRICES =
            Map.of(
                    FiatCurrency.EUR.getCode(),
                    BigDecimal.ONE,
                    FiatCurrency.USD.getCode(),
                    USD_TO_EUR,
                    StockMarketCurrency.AAPL.getCode(),
                    AAPL_PRICE.multiply(USD_TO_EUR),
                    CryptoCurrency.BTC.getCode(),
                    BTC_PRICE.multiply(USD_TO_EUR),
                    CryptoCurrency.ETH.getCode(),
                    ETH_PRICE.multiply(USD_TO_EUR),
                    CryptoCurrency.USDT.getCode(),
                    USD_TO_EUR);

    private DefaultInvestmentController controller;
    private InvestmentAccount account;
    private CashAccount cashAccount;
    private CurrencyConverter converter;

    /** Creates independent accounts and services before each test. */
    @BeforeEach
    void setUp() {
        converter = DefaultInvestmentControllerTest::convertAtFixedRate;

        account = new InvestmentAccount("Test Portfolio", FiatCurrency.USD, converter);

        cashAccount = new CashAccount("Test Cash", FiatCurrency.USD);
        cashAccount.addTransaction(
                CashTransaction.of(
                        Asset.of(FiatCurrency.USD, INITIAL_CASH),
                        Category.SAVINGS,
                        TEST_DATE,
                        "Initial deposit",
                        ""));

        final InvestmentAccountService investmentService = new InvestmentAccountService();
        investmentService.addWallet(account);
        investmentService.selectWallet(account.getId());

        final CashAccountService cashService = new CashAccountService();
        cashService.addWallet(cashAccount);
        cashService.selectWallet(cashAccount.getId());

        final Settings settings = new Settings();
        settings.setBaseCurrency(FiatCurrency.EUR.getCode());

        controller =
                new DefaultInvestmentController(
                        investmentService,
                        cashService,
                        converter,
                        settings,
                        new CSVInvestmentsSnapshotService());
    }

    @Test
    void testGetAllInvestmentAccounts() {
        assertEquals(1, controller.getAllInvestmentAccounts().size());
    }

    @Test
    void testGetCurrentInvestmentAccountIsPresent() {
        assertTrue(controller.getCurrentInvestmentAccount().isPresent());
    }

    @Test
    void testSelectWalletWithValidId() {
        assertTrue(controller.selectWallet(account.getId()));
    }

    @Test
    void testSelectWalletWithUnknownIdReturnsFalse() {
        assertFalse(controller.selectWallet(UUID.randomUUID()));
    }

    @Test
    void testGetCurrentBalanceEmptyAccount() {
        assertAmountEquals(BigDecimal.ZERO, controller.getCurrentBalance().amount());
    }

    @Test
    void testGetCurrentBalanceAfterBuy() {
        buyAapl(BigDecimal.TEN, AAPL_PRICE);

        assertAmountEquals(
                AAPL_PRICE.multiply(BigDecimal.TEN), controller.getCurrentBalance().amount());
    }

    @Test
    void testGetAggregatedBalanceInBaseCurrency() {
        buyAapl(BigDecimal.TEN, AAPL_PRICE);

        final Asset balance = controller.getAggregatedBalance();

        assertEquals(FiatCurrency.EUR.getCode(), balance.currency().getCode());
        assertAmountEquals(
                AAPL_PRICE.multiply(BigDecimal.TEN).multiply(USD_TO_EUR), balance.amount());
    }

    @Test
    void testGetAllTradeableAssetsContainsCryptoAndStocks() {
        final var assets = controller.getAllTradeableAssets();

        assertFalse(assets.isEmpty());
        assertTrue(assets.contains(CryptoCurrency.BTC));
        assertTrue(assets.contains(CryptoCurrency.ETH));
        assertTrue(assets.contains(StockMarketCurrency.AAPL));
    }

    @Test
    void testGetAllOwnedAssetsEmptyWhenNoPositions() {
        assertTrue(controller.getAllOwnedAssets().isEmpty());
    }

    @Test
    void testGetAllOwnedAssetsAfterBuy() {
        buyAapl(BigDecimal.TEN, AAPL_PRICE);

        final var owned = controller.getAllOwnedAssets();

        assertEquals(1, owned.size());
        assertTrue(owned.contains(StockMarketCurrency.AAPL));
    }

    @Test
    void testGetOwnedStableCoinsEmptyWhenNoneOwned() {
        assertTrue(controller.getOwnedStableCoins().isEmpty());
    }

    @Test
    void testGetOwnedStableCoinsAfterBuyingUsdt() {
        account.addTransaction(
                InvestmentTransaction.of(
                        Asset.of(CryptoCurrency.USDT, STABLECOIN_QUANTITY),
                        Category.INVESTMENT_BUY,
                        TEST_DATE,
                        "",
                        "",
                        Asset.of(FiatCurrency.USD, BigDecimal.ONE),
                        Asset.zero(FiatCurrency.USD)));

        final var stableCoins = controller.getOwnedStableCoins();

        assertEquals(1, stableCoins.size());
        assertTrue(stableCoins.contains(CryptoCurrency.USDT));
    }

    @Test
    void testEstimateBuyOrderCostFromCash() {
        final Asset cost =
                controller.estimateOrderCost(
                        OrderType.BUY,
                        StockMarketCurrency.AAPL,
                        TWO_UNITS,
                        Asset.of(FiatCurrency.USD, AAPL_PRICE),
                        Asset.of(FiatCurrency.USD, ORDER_FEE),
                        new PaymentSource.CashAccountChannel(cashAccount));

        assertEquals(FiatCurrency.USD, cost.currency());
        assertAmountEquals(AAPL_PRICE.multiply(TWO_UNITS).add(ORDER_FEE), cost.amount());
    }

    @Test
    void testEstimateSellOrderCostFromCash() {
        final Asset proceeds =
                controller.estimateOrderCost(
                        OrderType.SELL,
                        StockMarketCurrency.AAPL,
                        TWO_UNITS,
                        Asset.of(FiatCurrency.USD, AAPL_PRICE),
                        Asset.of(FiatCurrency.USD, ORDER_FEE),
                        new PaymentSource.CashAccountChannel(cashAccount));

        assertEquals(FiatCurrency.USD, proceeds.currency());
        assertAmountEquals(AAPL_PRICE.multiply(TWO_UNITS).subtract(ORDER_FEE), proceeds.amount());
    }

    @Test
    void testEstimateTransferOrderCostEqualsQuantity() {
        final Asset cost =
                controller.estimateOrderCost(
                        OrderType.TRANSFER,
                        CryptoCurrency.BTC,
                        HALF_UNIT,
                        Asset.zero(FiatCurrency.USD),
                        Asset.zero(FiatCurrency.USD),
                        new PaymentSource.NoPaymentChannel());

        assertEquals(CryptoCurrency.BTC, cost.currency());
        assertAmountEquals(HALF_UNIT, cost.amount());
    }

    @Test
    void testCanBuyWithSufficientCash() {
        assertTrue(
                controller.canBuy(
                        new PaymentSource.CashAccountChannel(cashAccount),
                        StockMarketCurrency.AAPL,
                        BigDecimal.TEN,
                        Asset.of(FiatCurrency.USD, AAPL_PRICE),
                        Asset.of(FiatCurrency.USD, ORDER_FEE)));
    }

    @Test
    void testCanBuyWithInsufficientCash() {
        assertFalse(
                controller.canBuy(
                        new PaymentSource.CashAccountChannel(cashAccount),
                        CryptoCurrency.BTC,
                        LARGE_QUANTITY,
                        Asset.of(FiatCurrency.USD, BTC_PRICE),
                        Asset.zero(FiatCurrency.USD)));
    }

    @Test
    void testCanBuyWithNoPaymentSourceAlwaysTrue() {
        assertTrue(
                controller.canBuy(
                        new PaymentSource.NoPaymentChannel(),
                        StockMarketCurrency.AAPL,
                        BigDecimal.TEN,
                        Asset.of(FiatCurrency.USD, AAPL_PRICE),
                        Asset.zero(FiatCurrency.USD)));
    }

    @Test
    void testCanSellWithSufficientPosition() {
        buyBtc(BigDecimal.TEN);

        assertTrue(controller.canSell(account, CryptoCurrency.BTC, BigDecimal.ONE));
    }

    @Test
    void testCanSellWithInsufficientPosition() {
        buyBtc(BigDecimal.ONE);

        assertFalse(controller.canSell(account, CryptoCurrency.BTC, BigDecimal.TEN));
    }

    @Test
    void testCanSellWithNoPositionReturnsFalse() {
        assertFalse(controller.canSell(account, CryptoCurrency.BTC, BigDecimal.ONE));
    }

    @Test
    void testCanTransferToSameAccountReturnsFalse() {
        buyBtc(BigDecimal.ONE);

        assertFalse(controller.canTransfer(account, account, CryptoCurrency.BTC, BigDecimal.ONE));
    }

    @Test
    void testCanTransferBetweenDifferentAccountsWithSufficientPosition() {
        buyBtc(BigDecimal.TEN);

        final InvestmentAccount other = createOtherAccount();

        assertTrue(controller.canTransfer(account, other, CryptoCurrency.BTC, BigDecimal.ONE));
    }

    @Test
    void testCanTransferWithInsufficientPositionReturnsFalse() {
        buyBtc(BigDecimal.ONE);

        final InvestmentAccount other = createOtherAccount();

        assertFalse(controller.canTransfer(account, other, CryptoCurrency.BTC, BigDecimal.TEN));
    }

    @Test
    void testExecuteBuyOrderWithCashSuccess() {
        final var result =
                controller.executeBuyOrder(
                        account,
                        new PaymentSource.CashAccountChannel(cashAccount),
                        StockMarketCurrency.AAPL,
                        BigDecimal.TEN,
                        Asset.of(FiatCurrency.USD, AAPL_PRICE),
                        Asset.of(FiatCurrency.USD, ORDER_FEE),
                        TEST_DATE,
                        "Buy AAPL");

        assertInstanceOf(OrderResult.BuyWithCashSuccess.class, result);

        assertAmountEquals(
                BigDecimal.TEN,
                account.getPositionForAsset(StockMarketCurrency.AAPL).orElseThrow().quantity());

        final BigDecimal expectedCash =
                INITIAL_CASH.subtract(AAPL_PRICE.multiply(BigDecimal.TEN).add(ORDER_FEE));

        assertAmountEquals(expectedCash, cashAccount.getBalance().amount());
    }

    @Test
    void testExecuteBuyOrderWithInsufficientFundsReturnsError() {
        final var result =
                controller.executeBuyOrder(
                        account,
                        new PaymentSource.CashAccountChannel(cashAccount),
                        CryptoCurrency.BTC,
                        LARGE_QUANTITY,
                        Asset.of(FiatCurrency.USD, BTC_PRICE),
                        Asset.zero(FiatCurrency.USD),
                        TEST_DATE,
                        "");

        assertInstanceOf(OrderResult.InsufficientFunds.class, result);
        assertTrue(account.getPositions().isEmpty());
        assertAmountEquals(INITIAL_CASH, cashAccount.getBalance().amount());
    }

    @Test
    void testExecuteSellOrderWithCashSuccess() {
        buyAapl(BigDecimal.TEN, PURCHASE_BASIS_PRICE);

        final var result =
                controller.executeSellOrder(
                        account,
                        new PaymentSource.CashAccountChannel(cashAccount),
                        StockMarketCurrency.AAPL,
                        FIVE_UNITS,
                        Asset.of(FiatCurrency.USD, AAPL_PRICE),
                        Asset.of(FiatCurrency.USD, ORDER_FEE),
                        TEST_DATE,
                        "Partial sell");

        assertInstanceOf(OrderResult.SellWithCashSuccess.class, result);

        assertAmountEquals(
                FIVE_UNITS,
                account.getPositionForAsset(StockMarketCurrency.AAPL).orElseThrow().quantity());

        final BigDecimal expectedCash =
                INITIAL_CASH.add(AAPL_PRICE.multiply(FIVE_UNITS).subtract(ORDER_FEE));

        assertAmountEquals(expectedCash, cashAccount.getBalance().amount());
    }

    @Test
    void testExecuteSellOrderWithNoPositionReturnsError() {
        final var result =
                controller.executeSellOrder(
                        account,
                        new PaymentSource.CashAccountChannel(cashAccount),
                        CryptoCurrency.BTC,
                        BigDecimal.ONE,
                        Asset.of(FiatCurrency.USD, BTC_PRICE),
                        Asset.zero(FiatCurrency.USD),
                        TEST_DATE,
                        "");

        assertInstanceOf(OrderResult.InsufficientAssets.class, result);
    }

    @Test
    void testExecuteTransferOrderSuccess() {
        buyBtc(BigDecimal.TEN);

        final InvestmentAccount other = createOtherAccount();

        final var result =
                controller.executeTransferOrder(
                        account, other, CryptoCurrency.BTC, BigDecimal.ONE, TEST_DATE, "");

        assertInstanceOf(OrderResult.TransferSuccess.class, result);

        assertAmountEquals(
                NINE_UNITS,
                account.getPositionForAsset(CryptoCurrency.BTC).orElseThrow().quantity());

        assertAmountEquals(
                BigDecimal.ONE,
                other.getPositionForAsset(CryptoCurrency.BTC).orElseThrow().quantity());
    }

    @Test
    void testExecuteTransferOrderToSameAccountReturnsError() {
        buyBtc(BigDecimal.ONE);

        final var result =
                controller.executeTransferOrder(
                        account, account, CryptoCurrency.BTC, BigDecimal.ONE, TEST_DATE, "");

        assertInstanceOf(OrderResult.InsufficientAssets.class, result);
    }

    @Test
    void testExecuteTransferOrderWithInsufficientPositionReturnsError() {
        buyBtc(BigDecimal.ONE);

        final InvestmentAccount other = createOtherAccount();

        final var result =
                controller.executeTransferOrder(
                        account, other, CryptoCurrency.BTC, BigDecimal.TEN, TEST_DATE, "");

        assertInstanceOf(OrderResult.InsufficientAssets.class, result);
    }

    /**
     * Converts an amount using deterministic reference prices.
     *
     * @param amount the amount to convert
     * @param from the source currency
     * @param to the target currency
     * @return the conversion result
     */
    private static CurrencyConversionResult convertAtFixedRate(
            final BigDecimal amount, final CurrencyUnit from, final CurrencyUnit to) {

        if (from.getCode().equalsIgnoreCase(to.getCode())) {
            return new CurrencyConversionResult(amount, from, to, BigDecimal.ONE, amount);
        }

        final BigDecimal rate =
                referencePrice(from).divide(referencePrice(to), RATE_SCALE, RoundingMode.HALF_UP);

        final BigDecimal convertedAmount =
                amount.multiply(rate).setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        return new CurrencyConversionResult(amount, from, to, rate, convertedAmount);
    }

    /**
     * Retrieves the reference price of a supported currency.
     *
     * @param currency the currency to price
     * @return its price in the reference currency
     */
    private static BigDecimal referencePrice(final CurrencyUnit currency) {
        final BigDecimal price = REFERENCE_PRICES.get(currency.getCode());

        if (price == null) {
            throw new IllegalArgumentException(
                    "No test price configured for " + currency.getCode());
        }

        return price;
    }

    /**
     * Compares monetary amounts independently of their scale.
     *
     * @param expected the expected amount
     * @param actual the actual amount
     */
    private static void assertAmountEquals(final BigDecimal expected, final BigDecimal actual) {

        assertEquals(expected.stripTrailingZeros(), actual.stripTrailingZeros());
    }

    /**
     * Creates another account for transfer tests.
     *
     * @return an account using the same fixed-rate converter
     */
    private InvestmentAccount createOtherAccount() {
        return new InvestmentAccount(OTHER_ACCOUNT_NAME, FiatCurrency.USD, converter);
    }

    /**
     * Adds an AAPL purchase directly to the investment account.
     *
     * @param quantity the purchased quantity
     * @param pricePerShare the purchase price per share
     */
    private void buyAapl(final BigDecimal quantity, final BigDecimal pricePerShare) {

        account.addTransaction(
                InvestmentTransaction.of(
                        Asset.of(StockMarketCurrency.AAPL, quantity),
                        Category.INVESTMENT_BUY,
                        TEST_DATE,
                        "",
                        "",
                        Asset.of(FiatCurrency.USD, pricePerShare),
                        Asset.zero(FiatCurrency.USD)));
    }

    /**
     * Adds a BTC purchase directly to the investment account.
     *
     * @param quantity the purchased quantity
     */
    private void buyBtc(final BigDecimal quantity) {
        account.addTransaction(
                InvestmentTransaction.of(
                        Asset.of(CryptoCurrency.BTC, quantity),
                        Category.INVESTMENT_BUY,
                        TEST_DATE,
                        "",
                        "",
                        Asset.of(FiatCurrency.USD, BTC_PRICE),
                        Asset.zero(FiatCurrency.USD)));
    }
}
