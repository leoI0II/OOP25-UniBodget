package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a wallet (cash or investment) is added to its wallet service.
 *
 * <p>Unlike {@link NewWalletAddedEvent}, used by the investments view to refresh itself,
 * this event carries the wallet and is published for every wallet type.</p>
 *
 * @param wallet the wallet that was added
 */
public record WalletAddedEvent(AbstractWallet<?> wallet) implements MessageEvent { }
