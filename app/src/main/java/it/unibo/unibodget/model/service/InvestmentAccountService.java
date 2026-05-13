package it.unibo.unibodget.model.service;

import java.util.List;

import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

/**
 * A concrete {@link DefaultWalletService} specialized for {@link InvestmentAccount} wallets.
 *
 * <p>Locks the generic type parameters to {@code <InvestmentTransaction, InvestmentAccount>},
 * so callers do not need to specify them explicitly.
 */
public final class InvestmentAccountService extends DefaultWalletService<InvestmentTransaction, InvestmentAccount> {

    /**
     * Creates an empty investment account service with no wallets.
     */
    public InvestmentAccountService() {
        super();
    }

    /**
     * Creates an investment account service initialized with the given wallets.
     * The first wallet in the list becomes the current wallet.
     *
     * @param initialWallets the initial {@link InvestmentAccount} instances
     */
    public InvestmentAccountService(final InvestmentAccount... initialWallets) {
        super(List.of(initialWallets));
    }

}
