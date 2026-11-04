package it.unibo.unibodget.model.utils.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.transactions.base.AbstractTransaction;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a transaction of a wallet history is replaced by an edited one.
 *
 * @param wallet the wallet whose history changed
 * @param oldTransaction the transaction that was replaced
 * @param newTransaction the transaction that took its place
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Event payload carries wallet entity reference")
public record TransactionReplacedEvent(
        AbstractWallet<?> wallet,
        AbstractTransaction oldTransaction,
        AbstractTransaction newTransaction)
        implements MessageEvent { }
