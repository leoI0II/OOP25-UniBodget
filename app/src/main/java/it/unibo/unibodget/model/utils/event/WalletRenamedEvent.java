package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * An event triggered when a wallet (cash or investment) has been renamed.
 *
 * @param wallet  the wallet that was renamed (already carrying the new name)
 * @param oldName the name before the change
 * @param newName the name after the change
 */
public record WalletRenamedEvent(AbstractWallet<?> wallet, String oldName, String newName) implements MessageEvent { }
