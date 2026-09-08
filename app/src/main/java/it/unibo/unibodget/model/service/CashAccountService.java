package it.unibo.unibodget.model.service;

import java.util.List;

import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Concrete {@link DefaultWalletService} specialization for {@link CashAccount}
 * wallets.
 *
 * <p>
 * This class locks the generic type parameters to
 * {@code <CashTransaction, CashAccount>} so callers can work with a dedicated
 * service type without repeatedly specifying generic arguments.
 * </p>
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
     *
     * <p>
     * The first wallet in the list becomes the current wallet according to the
     * behavior defined by the superclass.
     * </p>
     *
     * @param initialWallets
     *            the initial {@link CashAccount} instances
     */
    public CashAccountService(final CashAccount... initialWallets) {
        super(List.of(initialWallets));
    }
}