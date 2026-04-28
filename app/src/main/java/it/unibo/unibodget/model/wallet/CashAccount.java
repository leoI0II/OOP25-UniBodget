package it.unibo.unibodget.model.wallet;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * A wallet that holds {@link CashTransaction} entries and computes its balance
 * by summing all transaction amounts in the base currency.
 */
public class CashAccount extends Wallet<CashTransaction> {

    /**
     * Creates a CashAccount with an existing transaction history.
     *
     * @param name         the display name; if empty a default name is generated
     * @param baseCurrency the currency in which the balance is expressed
     * @param history      the pre-existing transaction ledger
     */
    public CashAccount(String name, CurrencyUnit baseCurrency, Historical<CashTransaction> history) {
        super(name, baseCurrency, history, "Cash Account");
    }

    /**
     * Creates a CashAccount with an empty transaction history.
     *
     * @param name         the display name; if empty a default name is generated
     * @param baseCurrency the currency in which the balance is expressed
     */
    public CashAccount(String name, CurrencyUnit baseCurrency) {
        this(name, baseCurrency, new Historical<>());
    }

    /**
     * Computes the balance by summing the amounts of all recorded transactions.
     * A positive result indicates net income; negative indicates net expense.
     *
     * @return an {@link Asset} representing the total balance in the base currency
     */
    @Override
    public Asset getBalance() {
        BigDecimal total = getHistory().getTransactions().stream()
            .map(CashTransaction::getAsset)
            .map(Asset::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Asset.of(getBaseCurrency(), total);
    }

}
