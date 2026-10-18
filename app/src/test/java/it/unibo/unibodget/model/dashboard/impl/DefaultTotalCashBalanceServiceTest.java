package it.unibo.unibodget.model.dashboard.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyConversionResult;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Tests aggregated cash balances using deterministic conversion rates.
 */
final class DefaultTotalCashBalanceServiceTest {

    private static final String INCOME = "Income";
    private static final BigDecimal INITIAL_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal SECOND_AMOUNT = new BigDecimal("50.00");
    private static final BigDecimal USD_TO_EUR = new BigDecimal("0.90");
    private static final BigDecimal EUR_TO_USD = new BigDecimal("1.10");

    private CashAccountService walletService;
    private DefaultTotalCashBalanceService totalBalanceService;

    /**
     * Initializes independent services before each test.
     */
    @BeforeEach
    void setUp() {
        walletService = new CashAccountService();

        final CurrencyConverter converter =
                DefaultTotalCashBalanceServiceTest::convertAtFixedRate;

        totalBalanceService = new DefaultTotalCashBalanceService(
                walletService,
                converter
        );
    }

    @Test
    void shouldSumBalancesAlreadyInTargetCurrency() {
        addFundedWallet("Wallet 1", FiatCurrency.EUR, INITIAL_AMOUNT);
        addFundedWallet("Wallet 2", FiatCurrency.EUR, SECOND_AMOUNT);

        final Asset total = totalBalanceService.getTotalBalanceIn(FiatCurrency.EUR);

        assertEquals(FiatCurrency.EUR.getCode(), total.currency().getCode());
        assertAmountEquals(INITIAL_AMOUNT.add(SECOND_AMOUNT), total.amount());
    }

    @Test
    void shouldConvertBalancesBeforeSumming() {
        addFundedWallet("EUR wallet", FiatCurrency.EUR, INITIAL_AMOUNT);
        addFundedWallet("USD wallet", FiatCurrency.USD, INITIAL_AMOUNT);

        final Asset total = totalBalanceService.getTotalBalanceIn(FiatCurrency.EUR);

        assertEquals(FiatCurrency.EUR.getCode(), total.currency().getCode());
        assertAmountEquals(
                INITIAL_AMOUNT.add(INITIAL_AMOUNT.multiply(USD_TO_EUR)),
                total.amount()
        );
    }

    @Test
    void shouldReturnZeroWhenNoWalletsAreAvailable() {
        final Asset total = totalBalanceService.getTotalBalanceIn(FiatCurrency.EUR);

        assertEquals(FiatCurrency.EUR.getCode(), total.currency().getCode());
        assertAmountEquals(BigDecimal.ZERO, total.amount());
    }

    /**
     * Adds a wallet containing a single income transaction.
     *
     * @param name the wallet name
     * @param currency the wallet currency
     * @param amount the income amount
     */
    private void addFundedWallet(
            final String name,
            final CurrencyUnit currency,
            final BigDecimal amount) {

        final CashAccount wallet = new CashAccount(name, currency);
        walletService.addWallet(wallet);
        walletService.selectWallet(wallet.getId());

        final Category income = new Category(
                INCOME,
                ARGBColor.WHITE,
                CategoryType.INCOME
        );

        walletService.addTransaction(new CashTransaction(
                Asset.of(currency, amount),
                income,
                LocalDate.now(),
                "Deposit",
                INCOME
        ));
    }

    /**
     * Converts amounts using fixed test rates.
     *
     * @param amount the source amount
     * @param from the source currency
     * @param to the target currency
     * @return the conversion result
     */
    private static CurrencyConversionResult convertAtFixedRate(
            final BigDecimal amount,
            final CurrencyUnit from,
            final CurrencyUnit to) {

        final String sourceCode = from.getCode();
        final String targetCode = to.getCode();
        final BigDecimal rate;

        if (sourceCode.equals(targetCode)) {
            rate = BigDecimal.ONE;
        } else if (sourceCode.equals(FiatCurrency.USD.getCode())
                && targetCode.equals(FiatCurrency.EUR.getCode())) {
            rate = USD_TO_EUR;
        } else if (sourceCode.equals(FiatCurrency.EUR.getCode())
                && targetCode.equals(FiatCurrency.USD.getCode())) {
            rate = EUR_TO_USD;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported test conversion: " + sourceCode + " to " + targetCode
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
