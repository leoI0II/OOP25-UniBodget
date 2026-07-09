package it.unibo.unibodget.model.dashboard.api;

import java.util.List;

import it.unibo.unibodget.model.dashboard.impl.FriendLoanSummary;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Service responsible for computing friend-loan summaries
 * from cash transactions.
 */
public interface FriendLoanSummaryService {

    /**
     * Computes one summary for each friend loan found in the provided transactions.
     *
     * @param transactions
     *            the cash transactions to inspect
     * @return the computed friend-loan summaries
     */
    List<FriendLoanSummary> summarize(List<CashTransaction> transactions);
}