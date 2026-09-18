package it.unibo.unibodget.model.wallet;

import java.math.BigDecimal;
import java.util.Objects;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Cash wallet aggregate root.
 *
 * <p>It owns its transaction history and its budget settings.</p>
 */
public final class CashAccount extends AbstractWallet<CashTransaction> {

    private DefaultBudgetSettings budgetSettings;

    /**
     * Constructs a new {@code CashAccount} with the specified parameters.
     *
     * @param name           the name of the cash account
     * @param baseCurrency   the base currency unit of the account
     * @param history        the historical record of cash transactions
     * @param budgetSettings the budget settings associated with this account
     */
    public CashAccount(
            final String name,
            final CurrencyUnit baseCurrency,
            final Historical<CashTransaction> history,
            final DefaultBudgetSettings budgetSettings) {
        super(name, baseCurrency, history, "Cash Account");
        this.budgetSettings = Objects.requireNonNull(budgetSettings);
    }

    /**
     * Constructs a new {@code CashAccount} with the specified name and base currency,
     * initializing it with an empty transaction history and zero-budget settings.
     *
     * @param name         the name of the cash account
     * @param baseCurrency the base currency unit of the account
     */
    public CashAccount(final String name, final CurrencyUnit baseCurrency) {
        this(
                name,
                baseCurrency,
                new Historical<>(),
                new DefaultBudgetSettings(BigDecimal.ZERO)
        );
    }

    /**
     * Gets the budget settings for this cash account.
     *
     * @return the current {@link DefaultBudgetSettings}
     */
    public DefaultBudgetSettings getBudgetSettings() {
        return budgetSettings;
    }

    /**
     * Sets the budget settings for this cash account.
     *
     * @param budgetSettings the new {@link DefaultBudgetSettings} to apply
     */
    public void setBudgetSettings(final DefaultBudgetSettings budgetSettings) {
        this.budgetSettings = Objects.requireNonNull(budgetSettings);
    }

    /**
     * Calculates and returns the current balance of the cash account
     * by summing up all the transactions in its history.
     *
     * @return the total balance as an {@link Asset}
     */
    @Override
    public Asset getBalance() {
        return getHistory().getTransactions().stream()
                .map(CashTransaction::getAsset)
                .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }
}
