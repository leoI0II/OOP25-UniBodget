package it.unibo.unibodget.model.utils.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.transactions.base.AbstractTransaction;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a transaction is removed from a wallet history.
 *
 * @param wallet the wallet the transaction was removed from
 * @param transaction the removed transaction
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Event payload carries wallet and transaction entity references")
public record TransactionRemovedEvent(AbstractWallet<?> wallet, AbstractTransaction transaction)
        implements MessageEvent { }
