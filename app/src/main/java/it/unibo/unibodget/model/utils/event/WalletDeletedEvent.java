package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a wallet (cash or investment) has been removed from its service.
 *
 * @param wallet the wallet that was removed, together with its whole transaction history
 */
public record WalletDeletedEvent(AbstractWallet<?> wallet) implements MessageEvent { }
