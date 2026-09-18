package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.transactions.base.AbstractTransaction;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a new transaction is successfully added to a wallet.
 * 
 * @param wallet      the wallet to which the transaction was added
 * @param transaction the transaction that was added
 */
public record TransactionAddedEvent(AbstractWallet wallet, AbstractTransaction transaction) implements MessageEvent { }
