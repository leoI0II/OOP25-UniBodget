package it.unibo.unibodget.model.wallet;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Represents a cash‑based wallet containing a ledger of {@link CashTransaction}
 * and a configurable monthly budget. A {@code CashAccount} computes its balance
 * dynamically from its transaction history and stores user‑defined budget limits.
 * It is the aggregate root for all cash operations within UniBodget.
 */
public final class CashAccount extends Wallet<CashTransaction> {

    private DefaultBudgetSettings budgetSettings;

    /**
     * Creates a {@code CashAccount} from JSON data.
     * If {@code history} is null, an empty ledger is created.
     * If {@code budgetSettings} is null, a default configuration is applied.
     *
     * @param name the display name of the wallet
     * @param baseCurrency the currency used to express the balance
     * @param history the transaction ledger, or null for an empty one
     * @param budgetSettings the budget configuration, or null for defaults
     */
    @JsonCreator
    public CashAccount(
            @JsonProperty("name") final String name,
            @JsonProperty("baseCurrency") final CurrencyUnit baseCurrency,
            @JsonProperty("history") final Historical<CashTransaction> history,
            @JsonProperty("budgetSettings") final DefaultBudgetSettings budgetSettings) {

        super(
                name,
                baseCurrency,
                history != null ? history : new Historical<>(),
                "Cash Account"
        );
        this.budgetSettings = budgetSettings != null
                ? budgetSettings
                : new DefaultBudgetSettings(BigDecimal.ZERO);
    }

    /**
     * Creates a new {@code CashAccount} with an empty history and a default
     * budget limit of zero.
     *
     * @param name the display name of the wallet
     * @param baseCurrency the currency used to express the balance
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
     * Returns the current budget configuration associated with this wallet.
     *
     * @return the {@link DefaultBudgetSettings} instance
     */
    public DefaultBudgetSettings getBudgetSettings() {
        return budgetSettings;
    }

    /**
     * Updates the budget configuration for this wallet.
     *
     * @param budgetSettings the new settings to apply; must not be null
     * @throws NullPointerException if {@code budgetSettings} is null
     */
    public void setBudgetSettings(final DefaultBudgetSettings budgetSettings) {
        this.budgetSettings = Objects.requireNonNull(budgetSettings);
    }

    /**
     * Computes the current balance by summing all {@link Asset} values
     * contained in the transaction history. The balance is never cached.
     *
     * @return an {@link Asset} representing the total balance in the base currency
     */
    @Override
    public Asset getBalance() {
        return getHistory().getTransactions().stream()
                .map(CashTransaction::getAsset)
                .reduce(Asset.zero(getBaseCurrency()), Asset::add);
    }

}
