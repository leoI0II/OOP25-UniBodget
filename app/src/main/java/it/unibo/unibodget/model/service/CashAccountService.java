package it.unibo.unibodget.model.service;

import java.util.List;

import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * A concrete {@link DefaultWalletService} specialized for {@link CashAccount} wallets.
 *
 * <p>Locks the generic type parameters to {@code <CashTransaction, CashAccount>},
 * so callers do not need to specify them explicitly.
 */
public final class CashAccountService extends DefaultWalletService<CashTransaction, CashAccount> {

    /**
     * Creates an empty cash account service with no wallets.
     */
    public CashAccountService() {
        super();
    }

    /**
     * Creates a cash account service initialized with the given wallets.
     * The first wallet in the list becomes the current wallet.
     *
     * @param initialWallets the initial {@link CashAccount} instances
     */
    public CashAccountService(final CashAccount... initialWallets) {
        super(List.of(initialWallets));
    }

}
