package it.unibo.unibodget.investment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.converter.provider.MockExchangeRateProvider;
import it.unibo.unibodget.model.converter.provider.MockPriceProvider;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.controllers.DefaultInvestmentController;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

/**
 * Tests for DefaultInvestmentController.
 *
 * Fixed rates used (MockExchangeRateProvider / MockPriceProvider):
 *   AAPL = $150, BTC = $50 000, ETH = $4 000, USD→EUR = 0.91
 */
public class DefaultInvestmentControllerTest {

    private static final LocalDate TODAY = LocalDate.now();

    private DefaultInvestmentController controller;
    private InvestmentAccount account;
    private CashAccount cashAccount;
    private MockPriceProvider priceProvider;

    @BeforeEach
    void setUp() {
        priceProvider = new MockPriceProvider();

        account = new InvestmentAccount("Test Portfolio", FiatCurrency.USD, priceProvider);

        // Cash account funded with $10 000
        cashAccount = new CashAccount("Test Cash", FiatCurrency.USD);
        cashAccount.addTransaction(CashTransaction.of(
            Asset.of(FiatCurrency.USD, new BigDecimal("10000")),
            Category.SAVINGS, TODAY, "initial deposit", ""
        ));

        controller = new DefaultInvestmentController(
            new InvestmentAccountService(account),
            new CashAccountService(cashAccount),
            new MockExchangeRateProvider(),
            List.of(FiatCurrency.USD, FiatCurrency.EUR)
        );
    }

    // ------------------------------------------------------------------ //
    //  Account management                                                  //
    // ------------------------------------------------------------------ //

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

    // ------------------------------------------------------------------ //
    //  Balance queries                                                   //
    // ------------------------------------------------------------------ //

    @Test
    void testGetCurrentBalanceEmptyAccount() {
        assertEquals(
            BigDecimal.ZERO.stripTrailingZeros(),
            controller.getCurrentBalance().amount().stripTrailingZeros()
        );
    }

    @Test
    void testGetCurrentBalanceAfterBuy() {
        // 10 AAPL @ $150 market price -> balance = $1500
        buyAapl(BigDecimal.TEN, new BigDecimal("150"));
        assertEquals(
            new BigDecimal("1500").stripTrailingZeros(),
            controller.getCurrentBalance().amount().stripTrailingZeros()
        );
    }

    @Test
    void testGetAggregatedBalancesContainsBothCurrencies() {
        // 10 AAPL -> USD balance $1500, EUR balance 1500 * 0.91 = $1365
        buyAapl(BigDecimal.TEN, new BigDecimal("150"));
        var balances = controller.getAggregatedBalances();
        assertEquals(2, balances.size());
        assertEquals(
            new BigDecimal("1500").stripTrailingZeros(),
            balances.get(FiatCurrency.USD).amount().stripTrailingZeros()
        );
        assertEquals(
            new BigDecimal("1365").stripTrailingZeros(),
            balances.get(FiatCurrency.EUR).amount().stripTrailingZeros()
        );
    }

    // ------------------------------------------------------------------ //
    //  Asset queries                                                       //
    // ------------------------------------------------------------------ //

    @Test
    void testGetAllTradeableAssetsContainsCryptoAndStocks() {
        var assets = controller.getAllTradeableAssets();
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
        buyAapl(BigDecimal.TEN, new BigDecimal("150"));
        var owned = controller.getAllOwnedAssets();
        assertEquals(1, owned.size());
        assertTrue(owned.contains(StockMarketCurrency.AAPL));
    }

    @Test
    void testGetOwnedStableCoinsEmptyWhenNoneOwned() {
        assertTrue(controller.getOwnedStableCoins().isEmpty());
    }

    @Test
    void testGetOwnedStableCoinsAfterBuyingUsdt() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(CryptoCurrency.USDT, new BigDecimal("1000")),
            Category.INVESTMENT_BUY, TODAY, "", "",
            Asset.of(FiatCurrency.USD, BigDecimal.ONE),
            Asset.zero(FiatCurrency.USD)
        ));
        var stables = controller.getOwnedStableCoins();
        assertEquals(1, stables.size());
        assertTrue(stables.contains(CryptoCurrency.USDT));
    }

    // ------------------------------------------------------------------ //
    //  Order cost estimation                                             //
    // ------------------------------------------------------------------ //

    @Test
    void testEstimateBuyOrderCostFromCash() {
        // 2 AAPL @ $150 + $5 fee = $305
        var cost = controller.estimateOrderCost(
            OrderType.BUY, StockMarketCurrency.AAPL, BigDecimal.TWO,
            Asset.of(FiatCurrency.USD, new BigDecimal("150")),
            Asset.of(FiatCurrency.USD, new BigDecimal("5")),
            new PaymentSource.CashAccountChannel(cashAccount)
        );
        assertEquals(FiatCurrency.USD, cost.currency());
        assertEquals(new BigDecimal("305").stripTrailingZeros(), cost.amount().stripTrailingZeros());
    }

    @Test
    void testEstimateSellOrderCostFromCash() {
        // 2 AAPL @ $150 − $5 fee = $295 proceeds
        var proceeds = controller.estimateOrderCost(
            OrderType.SELL, StockMarketCurrency.AAPL, BigDecimal.TWO,
            Asset.of(FiatCurrency.USD, new BigDecimal("150")),
            Asset.of(FiatCurrency.USD, new BigDecimal("5")),
            new PaymentSource.CashAccountChannel(cashAccount)
        );
        assertEquals(FiatCurrency.USD, proceeds.currency());
        assertEquals(new BigDecimal("295").stripTrailingZeros(), proceeds.amount().stripTrailingZeros());
    }

    @Test
    void testEstimateTransferOrderCostEqualsQuantity() {
        // Transfer cost = quantity of the asset, no currency conversion
        var cost = controller.estimateOrderCost(
            OrderType.TRANSFER, CryptoCurrency.BTC, new BigDecimal("0.5"),
            Asset.zero(FiatCurrency.USD), Asset.zero(FiatCurrency.USD),
            new PaymentSource.NoPaymentChannel()
        );
        assertEquals(CryptoCurrency.BTC, cost.currency());
        assertEquals(new BigDecimal("0.5").stripTrailingZeros(), cost.amount().stripTrailingZeros());
    }

    // ------------------------------------------------------------------ //
    //  canBuy                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void testCanBuyWithSufficientCash() {
        // $10 000 available; 10 AAPL @ $150 + $5 fee = $1 505
        assertTrue(controller.canBuy(
            new PaymentSource.CashAccountChannel(cashAccount),
            StockMarketCurrency.AAPL, BigDecimal.TEN,
            Asset.of(FiatCurrency.USD, new BigDecimal("150")),
            Asset.of(FiatCurrency.USD, new BigDecimal("5"))
        ));
    }

    @Test
    void testCanBuyWithInsufficientCash() {
        // $10 000 available; 100 BTC @ $50 000 = $5 000 000
        assertFalse(controller.canBuy(
            new PaymentSource.CashAccountChannel(cashAccount),
            CryptoCurrency.BTC, BigDecimal.valueOf(100),
            Asset.of(FiatCurrency.USD, new BigDecimal("50000")),
            Asset.zero(FiatCurrency.USD)
        ));
    }

    @Test
    void testCanBuyWithNoPaymentSourceAlwaysTrue() {
        assertTrue(controller.canBuy(
            new PaymentSource.NoPaymentChannel(),
            StockMarketCurrency.AAPL, BigDecimal.TEN,
            Asset.of(FiatCurrency.USD, new BigDecimal("150")),
            Asset.zero(FiatCurrency.USD)
        ));
    }

    // ------------------------------------------------------------------ //
    //  canSell                                                           //
    // ------------------------------------------------------------------ //

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

    // ------------------------------------------------------------------ //
    //  canTransfer                                                       //
    // ------------------------------------------------------------------ //

    @Test
    void testCanTransferToSameAccountReturnsFalse() {
        buyBtc(BigDecimal.ONE);
        assertFalse(controller.canTransfer(account, account, CryptoCurrency.BTC, BigDecimal.ONE));
    }

    @Test
    void testCanTransferBetweenDifferentAccountsWithSufficientPosition() {
        buyBtc(BigDecimal.TEN);
        var other = new InvestmentAccount("Other", FiatCurrency.USD, priceProvider);
        assertTrue(controller.canTransfer(account, other, CryptoCurrency.BTC, BigDecimal.ONE));
    }

    @Test
    void testCanTransferWithInsufficientPositionReturnsFalse() {
        buyBtc(BigDecimal.ONE);
        var other = new InvestmentAccount("Other", FiatCurrency.USD, priceProvider);
        assertFalse(controller.canTransfer(account, other, CryptoCurrency.BTC, BigDecimal.TEN));
    }

    // ------------------------------------------------------------------ //
    //  executeBuyOrder                                                    //
    // ------------------------------------------------------------------ //

    @Test
    void testExecuteBuyOrderWithCashSuccess() {
        // Buy 10 AAPL @ $150 + $5 fee = $1505 debited from cash
        var result = controller.executeBuyOrder(
            account, new PaymentSource.CashAccountChannel(cashAccount),
            StockMarketCurrency.AAPL, BigDecimal.TEN,
            Asset.of(FiatCurrency.USD, new BigDecimal("150")),
            Asset.of(FiatCurrency.USD, new BigDecimal("5")),
            TODAY, "buy 10 AAPL"
        );
        assertInstanceOf(OrderResult.BuyWithCashSuccess.class, result);
        assertEquals(
            BigDecimal.TEN.stripTrailingZeros(),
            account.getPositionForAsset(StockMarketCurrency.AAPL).get().quantity().stripTrailingZeros()
        );
        // 10 000 − 1505 = 8495
        assertEquals(
            new BigDecimal("8495").stripTrailingZeros(),
            cashAccount.getBalance().amount().stripTrailingZeros()
        );
    }

    @Test
    void testExecuteBuyOrderWithInsufficientFundsReturnsError() {
        var result = controller.executeBuyOrder(
            account, new PaymentSource.CashAccountChannel(cashAccount),
            CryptoCurrency.BTC, BigDecimal.valueOf(100),
            Asset.of(FiatCurrency.USD, new BigDecimal("50000")),
            Asset.zero(FiatCurrency.USD),
            TODAY, ""
        );
        assertInstanceOf(OrderResult.InsufficientFunds.class, result);
        assertTrue(account.getPositions().isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  executeSellOrder                                                  //
    // ------------------------------------------------------------------ //

    @Test
    void testExecuteSellOrderWithCashSuccess() {
        // Hold 10 AAPL; sell 5 @ $150 − $5 fee = $745 credited to cash
        buyAapl(BigDecimal.TEN, new BigDecimal("100"));
        var result = controller.executeSellOrder(
            account, new PaymentSource.CashAccountChannel(cashAccount),
            StockMarketCurrency.AAPL, BigDecimal.valueOf(5),
            Asset.of(FiatCurrency.USD, new BigDecimal("150")),
            Asset.of(FiatCurrency.USD, new BigDecimal("5")),
            TODAY, "partial sell"
        );
        assertInstanceOf(OrderResult.SellWithCashSuccess.class, result);
        assertEquals(
            BigDecimal.valueOf(5).stripTrailingZeros(),
            account.getPositionForAsset(StockMarketCurrency.AAPL).get().quantity().stripTrailingZeros()
        );
        // 10 000 + 745 = 10 745
        assertEquals(
            new BigDecimal("10745").stripTrailingZeros(),
            cashAccount.getBalance().amount().stripTrailingZeros()
        );
    }

    @Test
    void testExecuteSellOrderWithNoPositionReturnsError() {
        var result = controller.executeSellOrder(
            account, new PaymentSource.CashAccountChannel(cashAccount),
            CryptoCurrency.BTC, BigDecimal.ONE,
            Asset.of(FiatCurrency.USD, new BigDecimal("50000")),
            Asset.zero(FiatCurrency.USD),
            TODAY, ""
        );
        assertInstanceOf(OrderResult.InsufficientAssets.class, result);
    }

    // ------------------------------------------------------------------ //
    //  executeTransferOrder                                               //
    // ------------------------------------------------------------------ //

    @Test
    void testExecuteTransferOrderSuccess() {
        buyBtc(BigDecimal.TEN);
        var other = new InvestmentAccount("Other", FiatCurrency.USD, priceProvider);
        var result = controller.executeTransferOrder(
            account, other, CryptoCurrency.BTC, BigDecimal.ONE, TODAY, ""
        );
        assertInstanceOf(OrderResult.TransferSuccess.class, result);
        assertEquals(
            BigDecimal.valueOf(9).stripTrailingZeros(),
            account.getPositionForAsset(CryptoCurrency.BTC).get().quantity().stripTrailingZeros()
        );
        assertEquals(
            BigDecimal.ONE.stripTrailingZeros(),
            other.getPositionForAsset(CryptoCurrency.BTC).get().quantity().stripTrailingZeros()
        );
    }

    @Test
    void testExecuteTransferOrderToSameAccountReturnsError() {
        buyBtc(BigDecimal.ONE);
        var result = controller.executeTransferOrder(
            account, account, CryptoCurrency.BTC, BigDecimal.ONE, TODAY, ""
        );
        assertInstanceOf(OrderResult.InsufficientAssets.class, result);
    }

    @Test
    void testExecuteTransferOrderWithInsufficientPositionReturnsError() {
        buyBtc(BigDecimal.ONE);
        var other = new InvestmentAccount("Other", FiatCurrency.USD, priceProvider);
        var result = controller.executeTransferOrder(
            account, other, CryptoCurrency.BTC, BigDecimal.TEN, TODAY, ""
        );
        assertInstanceOf(OrderResult.InsufficientAssets.class, result);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                            //
    // ------------------------------------------------------------------ //

    private void buyAapl(BigDecimal quantity, BigDecimal pricePerShare) {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, quantity),
            Category.INVESTMENT_BUY, TODAY, "", "",
            Asset.of(FiatCurrency.USD, pricePerShare),
            Asset.zero(FiatCurrency.USD)
        ));
    }

    private void buyBtc(BigDecimal quantity) {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(CryptoCurrency.BTC, quantity),
            Category.INVESTMENT_BUY, TODAY, "", "",
            Asset.of(FiatCurrency.USD, new BigDecimal("50000")),
            Asset.zero(FiatCurrency.USD)
        ));
    }
}
