package it.unibo.unibodget.model.utils.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a wallet (cash or investment) has been removed from its service.
 *
 * @param wallet the wallet that was removed, together with its whole transaction history
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Event payload carries wallet entity reference")
public record WalletDeletedEvent(AbstractWallet<?> wallet) implements MessageEvent { }
