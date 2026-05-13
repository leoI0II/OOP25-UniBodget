package it.unibo.unibodget.model.wallet;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Abstract base class representing a financial wallet that holds a ledger of transactions.
 *
 * <p>A wallet is parameterized on the transaction type it accepts, enforcing at compile time
 * that only compatible transactions can be added (e.g. a {@code CashAccount} only accepts
 * {@code CashTransaction} instances).
 *
 * <p>The balance is never stored as a field: subclasses must compute it dynamically from
 * the transaction history by implementing {@link #getBalance()}.
 *
 * @param <T> the type of {@link Transaction} this wallet accepts
 */
public abstract class Wallet<T extends Transaction> {

    private static final Map<String, AtomicInteger> nameCounters = new ConcurrentHashMap<>();
    
    private final UUID id;
    private String name;
    private final Historical<T> history;
    private final CurrencyUnit baseCurrency;

    /**
     * Creates a wallet with an existing transaction history.
     * If {@code name} is empty, a default name is generated using {@code typePrefix}
     * (e.g. "Cash Account 1", "Investment Account 2").
     *
     * @param name         the display name of this wallet; if empty a default is generated
     * @param baseCurrency the reference currency used to express the balance
     * @param history      the pre-existing transaction ledger
     * @param typePrefix   prefix used for the auto-generated name (supplied by the subclass)
     */
    protected Wallet(final String name, final CurrencyUnit baseCurrency,
                     final Historical<T> history, final String typePrefix) {
        this.id = UUID.randomUUID();
        this.name = name.isEmpty() ? generateDefaultName(typePrefix) : name;
        this.baseCurrency = baseCurrency;
        this.history = history;
    }

    /**
     * Creates a wallet with an empty transaction history.
     * If {@code name} is empty, a default name is generated using {@code typePrefix}.
     *
     * @param name         the display name of this wallet; if empty a default is generated
     * @param baseCurrency the reference currency used to express the balance
     * @param typePrefix   prefix used for the auto-generated name (supplied by the subclass)
     */
    protected Wallet(final String name, final CurrencyUnit baseCurrency, final String typePrefix) {
        this(name, baseCurrency, new Historical<>(), typePrefix);
    }

    // typePrefix is provided by the subclass because Java generics are erased at runtime:
    // Wallet cannot inspect T to determine whether it is CashTransaction or InvestmentTransaction.
    private static String generateDefaultName(final String typePrefix) {
        return typePrefix + " " + nameCounters
            .computeIfAbsent(typePrefix, k -> new AtomicInteger(0))
            .incrementAndGet();
    }

    /**
     * Returns the unique identifier of this wallet.
     *
     * @return the wallet {@link UUID}
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the display name of this wallet.
     *
     * @return the wallet name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this wallet.
     *
     * @param name the new name to assign
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the transaction history of this wallet.
     *
     * @return the {@link Historical} ledger
     */
    public Historical<T> getHistory() {
        return history;
    }

    /**
     * Returns the base currency in which the balance is expressed.
     *
     * @return the {@link CurrencyUnit} used as reference
     */
    public CurrencyUnit getBaseCurrency() {
        return baseCurrency;
    }

    /**
     * Adds a transaction to this wallet's history.
     *
     * @param transaction the transaction to record
     */
    public void addTransaction(final T transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        if (transaction.getAsset().isZero()) {
            throw new IllegalArgumentException("Transaction amount cannot be zero.");
        }
        history.addTransaction(transaction);
    }

    /**
     * Computes and returns the current balance of this wallet.
     * The balance is always derived from the transaction history, never cached.
     *
     * @return an {@link Asset} representing the current balance in the base currency
     */
    public abstract Asset getBalance();

}