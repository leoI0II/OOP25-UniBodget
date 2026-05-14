package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Tests for {@link DefaultTotalCashBalanceService}.
 */
class DefaultTotalCashBalanceServiceTest {

    private CashAccountService walletService;
    private CashBalanceConverter balanceConverter;
    private DefaultTotalCashBalanceService totalBalanceService;

    @BeforeEach
    void setUp() {
        walletService = new CashAccountService();
        balanceConverter = new StubCashBalanceConverter();
        totalBalanceService = new DefaultTotalCashBalanceService(walletService, balanceConverter);
    }

    @Test
    void shouldSumBalancesAlreadyInTargetCurrency() {
        final CashAccount firstWallet = new CashAccount("Wallet 1", FiatCurrency.EUR);
        final CashAccount secondWallet = new CashAccount("Wallet 2", FiatCurrency.EUR);

        walletService.addWallet(firstWallet);
        walletService.addWallet(secondWallet);

        final Category income = new Category(
                "Income",
                new ARGBColor(0xFF4CAF50),
                CategoryType.INCOME
        );

        walletService.selectWallet(firstWallet.getId());
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("100.00")),
                income,
                LocalDate.now(),
                "Salary",
                "Income"
        ));

        walletService.selectWallet(secondWallet.getId());
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("50.00")),
                income,
                LocalDate.now(),
                "Gift",
                "Income"
        ));

        final Asset total = totalBalanceService.getTotalBalanceIn(FiatCurrency.EUR);

        assertEquals(FiatCurrency.EUR, total.currency());
        assertEquals(new BigDecimal("150.00"), total.amount());
    }

    @Test
    void shouldConvertBalancesBeforeSumming() {
        final CashAccount eurWallet = new CashAccount("EUR wallet", FiatCurrency.EUR);
        final CashAccount usdWallet = new CashAccount("USD wallet", FiatCurrency.USD);

        walletService.addWallet(eurWallet);
        walletService.addWallet(usdWallet);

        final Category income = new Category(
                "Income",
                new ARGBColor(0xFF4CAF50),
                CategoryType.INCOME
        );

        walletService.selectWallet(eurWallet.getId());
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.EUR, new BigDecimal("100.00")),
                income,
                LocalDate.now(),
                "Salary",
                "Income"
        ));

        walletService.selectWallet(usdWallet.getId());
        walletService.addTransaction(new CashTransaction(
                Asset.of(FiatCurrency.USD, new BigDecimal("100.00")),
                income,
                LocalDate.now(),
                "Bonus",
                "Income"
        ));

        final Asset totalInEur = totalBalanceService.getTotalBalanceIn(FiatCurrency.EUR);

        assertEquals(FiatCurrency.EUR, totalInEur.currency());
        assertEquals(new BigDecimal("190.00").stripTrailingZeros(), totalInEur.amount().stripTrailingZeros());
    }

    @Test
    void shouldReturnZeroWhenNoWalletsAreAvailable() {
        final Asset total = totalBalanceService.getTotalBalanceIn(FiatCurrency.EUR);

        assertEquals(FiatCurrency.EUR, total.currency());
        assertEquals(BigDecimal.ZERO, total.amount());
    }

    private static final class StubCashBalanceConverter implements CashBalanceConverter {

        @Override
        public Asset convert(final Asset asset, final CurrencyUnit targetCurrency) {
            if (asset.currency().equals(targetCurrency)) {
                return asset;
            }

            if (asset.currency().equals(FiatCurrency.USD) && targetCurrency.equals(FiatCurrency.EUR)) {
                return Asset.of(targetCurrency, asset.amount().multiply(new BigDecimal("0.90")));
            }

            if (asset.currency().equals(FiatCurrency.EUR) && targetCurrency.equals(FiatCurrency.USD)) {
                return Asset.of(targetCurrency, asset.amount().multiply(new BigDecimal("1.10")));
            }

            throw new IllegalArgumentException("Unsupported test conversion");
        }
    }
}