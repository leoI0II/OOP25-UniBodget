package it.unibo.unibodget.model.utils.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.transactions.base.AbstractTransaction;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a new transaction is successfully added to a wallet.
 *
 * @param wallet the wallet to which the transaction was added
 * @param transaction the transaction that was added
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Event payload carries wallet and transaction entity references")
public record TransactionAddedEvent(AbstractWallet<?> wallet, AbstractTransaction transaction)
        implements MessageEvent { }
