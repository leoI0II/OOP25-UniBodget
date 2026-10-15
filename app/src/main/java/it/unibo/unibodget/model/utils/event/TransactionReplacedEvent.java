package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.transactions.base.AbstractTransaction;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a transaction of a wallet history is replaced by an edited one.
 *
 * @param wallet         the wallet whose history changed
 * @param oldTransaction the transaction that was replaced
 * @param newTransaction the transaction that took its place
 */
public record TransactionReplacedEvent(
        AbstractWallet<?> wallet,
        AbstractTransaction oldTransaction,
        AbstractTransaction newTransaction
) implements MessageEvent { }
