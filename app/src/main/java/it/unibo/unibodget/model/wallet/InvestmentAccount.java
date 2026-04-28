package it.unibo.unibodget.model.wallet;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;

/**
 * A wallet that holds {@link InvestmentTransaction} entries (stocks, crypto, funds).
 *
 * <p>The balance depends on current market prices and is not implemented yet:
 * it will be computed as Σ(quantity × current market price − fees) for each held asset.
 */
public class InvestmentAccount extends Wallet<InvestmentTransaction> {

    /**
     * Creates an InvestmentAccount with an existing transaction history.
     *
     * @param name         the display name; if empty a default name is generated
     * @param baseCurrency the currency in which the balance is expressed
     * @param history      the pre-existing transaction ledger
     */
    public InvestmentAccount(String name, CurrencyUnit baseCurrency, Historical<InvestmentTransaction> history) {
        super(name, baseCurrency, history, "Investment Account");
    }

    /**
     * Creates an InvestmentAccount with an empty transaction history.
     *
     * @param name         the display name; if empty a default name is generated
     * @param baseCurrency the currency in which the balance is expressed
     */
    public InvestmentAccount(String name, CurrencyUnit baseCurrency) {
        this(name, baseCurrency, new Historical<>());
    }

    /**
     * Computes the current balance of this investment account.
     * Will be implemented once market price integration is available.
     *
     * @return an {@link Asset} representing the total balance in the base currency
     */
    @Override
    public Asset getBalance() {
        return Asset.of(getBaseCurrency(), BigDecimal.ZERO);
    }

}
