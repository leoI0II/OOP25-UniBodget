package it.unibo.unibodget.model.dashboard.api;

import java.util.List;

import it.unibo.unibodget.model.dashboard.impl.WalletInsight;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Service responsible for computing high-level wallet insights
 * from a list of cash transactions.
 *
 * <p>Insights are intended for dashboard presentation and provide
 * user-facing messages about monthly spending, income, and savings
 * trends.</p>
 */
public interface WalletInsightService {

    /**
     * Computes the wallet insights for the provided transactions.
     *
     * @param transactions
     *            the transactions to inspect; must not be null
     * @return the computed insights, in display order
     */
    List<WalletInsight> computeInsights(List<CashTransaction> transactions);
}