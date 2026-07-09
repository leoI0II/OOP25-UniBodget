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
 * It owns its transaction history and its budget settings.
 */
public final class CashAccount extends Wallet<CashTransaction> {

    private DefaultBudgetSettings budgetSettings;

    public CashAccount(
            final String name,
            final CurrencyUnit baseCurrency,
            final Historical<CashTransaction> history,
            final DefaultBudgetSettings budgetSettings) {
        super(name, baseCurrency, history, "Cash Account");
        this.budgetSettings = Objects.requireNonNull(budgetSettings);
    }

    public CashAccount(final String name, final CurrencyUnit baseCurrency) {
        this(
                name,
                baseCurrency,
                new Historical<>(),
                new DefaultBudgetSettings(BigDecimal.ZERO)
        );
    }

    public DefaultBudgetSettings getBudgetSettings() {
        return budgetSettings;
    }

    public void setBudgetSettings(final DefaultBudgetSettings budgetSettings) {
        this.budgetSettings = Objects.requireNonNull(budgetSettings);
    }

    @Override
    public Asset getBalance() {
        return getHistory().getTransactions().stream()
                .map(CashTransaction::getAsset)
                .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }
}