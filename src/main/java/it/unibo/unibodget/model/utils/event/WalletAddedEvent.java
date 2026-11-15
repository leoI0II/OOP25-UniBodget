package it.unibo.unibodget.model.utils.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a wallet (cash or investment) is added to its wallet service.
 *
 * <p>Unlike {@link NewWalletAddedEvent}, used by the investments view to refresh itself, this event
 * carries the wallet and is published for every wallet type.
 *
 * @param wallet the wallet that was added
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Event payload carries wallet entity reference")
public record WalletAddedEvent(AbstractWallet<?> wallet) implements MessageEvent { }
