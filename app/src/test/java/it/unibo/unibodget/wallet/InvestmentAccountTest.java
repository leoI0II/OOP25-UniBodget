package it.unibo.unibodget.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.converter.provider.MockPriceProvider;
import it.unibo.unibodget.model.converter.provider.PriceProvider;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

public class InvestmentAccountTest {
    
    private InvestmentAccount account;
    private PriceProvider mockPriceProvider;

    @BeforeEach
    void setUp() {
        mockPriceProvider = new MockPriceProvider();
        account = new InvestmentAccount("Test Account", FiatCurrency.USD, mockPriceProvider);
    }

    @Test
    void testEmptyPositionsList() {
        assertEquals(0, account.getPositions().size());
    }

    @Test
    void testSingleBuy() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(CryptoCurrency.BTC, BigDecimal.ONE), // Buying 1 BTC
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("50000.00")),
            Asset.zero(FiatCurrency.USD)
        ));
        assertEquals(1, account.getPositions().size());

        assertEquals(BigDecimal.ONE, account.getPositions().get(0).quantity());
        assertEquals(
            new BigDecimal("50000.00").stripTrailingZeros(), 
            account.getPositions().get(0).averageBasisCost().amount().stripTrailingZeros()
        );
    }

    @Test
    void testTwoBuysOfOneAsset() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(CryptoCurrency.BTC, BigDecimal.ONE), // Buying 1 BTC
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("50000.00")),
            Asset.zero(FiatCurrency.USD)
        ));
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(CryptoCurrency.BTC, BigDecimal.ONE), // Buying 1 BTC
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("55000.00")),
            Asset.zero(FiatCurrency.USD)
        ));
        assertEquals(1, account.getPositions().size());
        assertEquals(BigDecimal.valueOf(2), account.getPositions().get(0).quantity());
        assertEquals(
            new BigDecimal("52500.00").stripTrailingZeros(), 
            account.getPositions().get(0).averageBasisCost().amount().stripTrailingZeros()
        );
    }

    @Test
    void testBuyNSellPosition() {
        account.addTransaction(
            InvestmentTransaction.of(
                Asset.of(CryptoCurrency.BTC, BigDecimal.ONE), // Buying 1 BTC
                Category.INVESTMENT_BUY,
                LocalDate.now(),
                "",
                "",
                Asset.of(FiatCurrency.USD, new BigDecimal("55000.00")),
                Asset.zero(FiatCurrency.USD)
            )
        );
        account.addTransaction(
            InvestmentTransaction.of(
                Asset.of(CryptoCurrency.BTC, BigDecimal.ONE.negate()),
                Category.INVESTMENT_SELL,
                LocalDate.now(),
                "",
                "",
                Asset.of(FiatCurrency.USD, new BigDecimal("55000.00")),
                Asset.zero(FiatCurrency.USD)
            )
        );
        assertEquals(0, account.getPositions().size());
    }

    @Test
    void testTwoDiffBuys() {
        account.addTransaction(
            InvestmentTransaction.of(
                Asset.of(CryptoCurrency.BTC, BigDecimal.ONE), // Buying 1 BTC
                Category.INVESTMENT_BUY,
                LocalDate.now(),
                "",
                "",
                Asset.of(FiatCurrency.USD, new BigDecimal("55000.00")),
                Asset.zero(FiatCurrency.USD)
            )
        );
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(CryptoCurrency.ETH, BigDecimal.TWO),
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("4000.0")),
            Asset.zero(FiatCurrency.USD)
        ));
        assertEquals(2, account.getPositions().size());
        assertEquals(BigDecimal.ONE, account.getPositionForAsset(CryptoCurrency.BTC).get().quantity());
        assertEquals(BigDecimal.TWO, account.getPositionForAsset(CryptoCurrency.ETH).get().quantity());
        assertEquals("BTC", account.getPositionForAsset(CryptoCurrency.BTC).get().asset().getShortName());
        assertEquals("ETH", account.getPositionForAsset(CryptoCurrency.ETH).get().asset().getShortName());
    }

    @Test
    void testEmptyWalletZeroBalance() {
        assertEquals(
            BigDecimal.ZERO.stripTrailingZeros(), 
            account.getBalance().amount().stripTrailingZeros()
        );
    }

    @Test
    void testBalanceAfterSingleBuy() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.TEN), // Buying 10 AAPL shares
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "test buy aapl",
            "notes",
            Asset.of(FiatCurrency.USD, BigDecimal.valueOf(150)),
            Asset.zero(FiatCurrency.USD)
        ));

        assertEquals(
            new BigDecimal("1500.00").stripTrailingZeros(), 
            account.getBalance().amount().stripTrailingZeros()
        );
    }

    @Test
    void testSingleBuyProfit() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.TEN), // Buying 10 AAPL shares
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "test buy aapl",
            "notes",
            Asset.of(FiatCurrency.USD, new BigDecimal("100.00")),
            Asset.zero(FiatCurrency.USD)
        ));
        var profit = account.getUnrealizedProfitLoss();
        assertEquals(
            new BigDecimal("500.00").stripTrailingZeros(),
            profit.amount().stripTrailingZeros()
        );
        assertEquals(
            new BigDecimal("500.00").stripTrailingZeros(),
            account.getPositions().get(0).getUnrealizedProfitLoss().amount().stripTrailingZeros()
        );
    }

    @Test
    void testSingleBuyLoss() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.TEN), // Buying 10 AAPL shares
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "test buy aapl",
            "notes",
            Asset.of(FiatCurrency.USD, new BigDecimal("200.00")),
            Asset.zero(FiatCurrency.USD)
        ));
        var profit = account.getUnrealizedProfitLoss();
        assertEquals(
            new BigDecimal("-500.00").stripTrailingZeros(),
            profit.amount().stripTrailingZeros()
        );
        assertEquals(
            new BigDecimal("-500.00").stripTrailingZeros(),
            account.getPositions().get(0).getUnrealizedProfitLoss().amount().stripTrailingZeros()
        );
    }

    @Test
    void testRealizedPLZero() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(CryptoCurrency.BTC, BigDecimal.ONE), // Buying 1 BTC
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("50000.00")),
            Asset.zero(FiatCurrency.USD)
        ));
        assertEquals(BigDecimal.ZERO,
            account.getRealizedProfitLoss().amount().stripTrailingZeros()
        );
    }

    @Test
    void testRealizedPLOneBuyOneSell() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.TEN),
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("100.0")),
            Asset.zero(FiatCurrency.USD)
        ));
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.valueOf(-5)), // Selling 5 AAPL shares
            Category.INVESTMENT_SELL,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("200.0")),
            Asset.zero(FiatCurrency.USD)
        ));
        assertEquals(
            new BigDecimal("500.00").stripTrailingZeros(),
            account.getRealizedProfitLoss().amount().stripTrailingZeros()
        );
    }

    @Test
    void testRealizedPLMultipleBuysSells() {
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.TEN),
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("100.0")),
            Asset.zero(FiatCurrency.USD)
        ));
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.valueOf(-5)),
            Category.INVESTMENT_SELL,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("80.0")),
            Asset.zero(FiatCurrency.USD)
        ));
        assertEquals(
            new BigDecimal("-100.0").stripTrailingZeros(),
            account.getRealizedProfitLoss().amount().stripTrailingZeros()
        );

        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.TEN),
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("100.0")),
            Asset.zero(FiatCurrency.USD)
        ));
        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.TEN),
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("150.0")),
            Asset.zero(FiatCurrency.USD)
        ));

        account.addTransaction(InvestmentTransaction.of(
            Asset.of(StockMarketCurrency.AAPL, BigDecimal.TEN.negate()),
            Category.INVESTMENT_BUY,
            LocalDate.now(),
            "",
            "",
            Asset.of(FiatCurrency.USD, new BigDecimal("200.0")),
            Asset.zero(FiatCurrency.USD)
        ));

        assertEquals(
            new BigDecimal("700.0").stripTrailingZeros(),
            account.getRealizedProfitLoss().amount().stripTrailingZeros()
        );

    }

}
