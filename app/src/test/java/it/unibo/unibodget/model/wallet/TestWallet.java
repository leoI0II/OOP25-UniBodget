package it.unibo.unibodget.model.wallet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.converter.provider.MockPriceProvider;
import it.unibo.unibodget.model.converter.provider.PriceProvider;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

/**
 * Class for test - used as main.
 */
public final class TestWallet {

        /**
         * To prevent instantiation.
         */
        private TestWallet() {

        }

    public static void main(final String[] args) {

        final PriceProvider provider = new MockPriceProvider();
        final CashAccount cash1 =
                new CashAccount(
                        "Main Wallet",
                        FiatCurrency.EUR
                );
        final CashAccount cash2 =
                new CashAccount(
                        "Travel Wallet",
                        FiatCurrency.EUR
                );

        final CashTransaction salary =
                CashTransaction.of(
                        Asset.of(
                                FiatCurrency.EUR,
                                BigDecimal.valueOf(2000)
                        ),
                        Category.SAVINGS,
                        LocalDate.now(),
                        "Salary",
                        null
                );
        final CashTransaction food =
                CashTransaction.of(
                        Asset.of(
                                FiatCurrency.EUR,
                                BigDecimal.valueOf(-50)
                        ),
                        Category.FOOD,
                        LocalDate.now(),
                        "Dinner",
                        null
                );
        final CashTransaction transport =
                CashTransaction.of(
                        Asset.of(
                                FiatCurrency.EUR,
                                BigDecimal.valueOf(-20)
                        ),
                        Category.TRANSPORT,
                        LocalDate.now(),
                        "Bus",
                        null
                );
        final CashTransaction trip =
                CashTransaction.of(
                        Asset.of(
                                FiatCurrency.EUR,
                                BigDecimal.valueOf(-300)
                        ),
                        Category.TRANSPORT,
                        LocalDate.now(),
                        "Trip",
                        null
                );

        cash1.addTransaction(salary);
        cash1.addTransaction(food);
        cash1.addTransaction(transport);
        cash2.addTransaction(trip);

        System.out.println("=== CASH ACCOUNT ===");
        System.out.println(
                "Name: "
                + cash1.getName()
        );
        System.out.println(
                "Currency: "
                + cash1.getBaseCurrency()
        );
        System.out.println(
                "History size: "
                + cash1.getHistory()
                        .getTransactions()
                        .size()
        );
        System.out.println(
                "Balance: "
                + cash1.getBalance()
        );
        System.out.println(
                "Budget settings: "
                + cash1.getBudgetSettings()
        );

        cash1.setBudgetSettings(
                cash1.getBudgetSettings()
        );
        System.out.println(
                "ID: "
                + cash1.getId()
        );

        final InvestmentAccount investments1 =
                new InvestmentAccount(
                        "Stocks Portfolio",
                        FiatCurrency.EUR,
                        provider
                );
        final InvestmentAccount investments2 =
                new InvestmentAccount(
                        "Crypto Portfolio",
                        FiatCurrency.EUR,
                        provider
                );

        final InvestmentTransaction buyApple =
                InvestmentTransaction.of(
                        Asset.of(
                                FiatCurrency.USD,
                                BigDecimal.valueOf(10)
                        ),
                        Category.INVESTMENT_BUY,
                        LocalDate.now(),
                        "Buy Apple",
                        null,
                        Asset.of(
                                FiatCurrency.USD,
                                BigDecimal.valueOf(150)
                        ),
                        Asset.of(
                                FiatCurrency.USD,
                                BigDecimal.valueOf(2)
                        )
                );
        final InvestmentTransaction sellApple =
                InvestmentTransaction.of(
                        Asset.of(
                                FiatCurrency.USD,
                                BigDecimal.valueOf(-5)
                        ),
                        Category.INVESTMENT_SELL,
                        LocalDate.now(),
                        "Sell Apple",
                        null,
                        Asset.of(
                                FiatCurrency.USD,
                                BigDecimal.valueOf(170)
                        ),
                        null
                );
        final InvestmentTransaction buyCrypto =
                InvestmentTransaction.of(
                        Asset.of(
                                FiatCurrency.USD,
                                BigDecimal.valueOf(3)
                        ),
                        Category.INVESTMENT_BUY,
                        LocalDate.now(),
                        "Buy crypto",
                        null,
                        Asset.of(
                                FiatCurrency.USD,
                                BigDecimal.valueOf(100)
                        ),
                        null
                );

        investments1.addTransaction(buyApple);
        investments1.addTransaction(sellApple);
        investments2.addTransaction(buyCrypto);

        System.out.println();
        System.out.println("=== INVESTMENT ACCOUNT ===");
        System.out.println(
                "Name: "
                + investments1.getName()
        );
        System.out.println(
                "Currency: "
                + investments1.getBaseCurrency()
        );
        System.out.println(
                "History size: "
                + investments1.getHistory()
                        .getTransactions()
                        .size()
        );
        System.out.println(
                "Positions: "
                + investments1.getPositions()
        );
        System.out.println(
                "Balance: "
                + investments1.getBalance()
        );
        System.out.println(
                "Position Apple: "
                + investments1.getPositionForAsset(
                        FiatCurrency.USD
                )
        );
        System.out.println(
                "Realized PL: "
                + investments1.getRealizedProfitLoss()
        );
        System.out.println(
                "Unrealized PL: "
                + investments1.getUnrealizedProfitLoss()
        );
        System.out.println(
                "Total PL: "
                + investments1.getTotalProfitLoss()
        );
        System.out.println(
                "PL percentage: "
                + investments1.getTotalProfitLossPercentage()
        );
        System.out.println(
                "Cost basis: "
                + investments1.getTotalCostBasis()
        );
        System.out.println(
                "Can sell 2 USD: "
                + investments1.canSell(
                        FiatCurrency.USD,
                        BigDecimal.valueOf(2)
                )
        );
        final InvestmentAccount copy =
                investments1.withProvider(provider);
        System.out.println(
                "Copy provider null? "
                + (copy.getPriceProvider() == null)
        );

        final CashAccountManager cashManager =
                new CashAccountManager();
        cashManager.saveAll(
                List.of(
                        cash1,
                        cash2
                )
        );
        final InvestmentAccountManager investmentManager =
                new InvestmentAccountManager(provider);
        investmentManager.saveAll(
                List.of(
                        investments1,
                        investments2
                )
        );

        System.out.println();
        System.out.println("=== LOAD TEST ===");
        System.out.println(
                "Cash loaded: "
                + cashManager.loadAll().size()
        );
        System.out.println(
                "Investment loaded: "
                + investmentManager.loadAll().size()
        );

    }

}
