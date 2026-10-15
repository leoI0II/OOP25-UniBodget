package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.transactions.base.AbstractTransaction;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a transaction is removed from a wallet history.
 *
 * @param wallet      the wallet the transaction was removed from
 * @param transaction the removed transaction
 */
public record TransactionRemovedEvent(
        AbstractWallet<?> wallet,
        AbstractTransaction transaction
) implements MessageEvent { }
