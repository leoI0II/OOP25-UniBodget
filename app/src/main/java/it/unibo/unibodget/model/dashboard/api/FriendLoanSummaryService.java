package it.unibo.unibodget.model.dashboard.api;

import it.unibo.unibodget.model.dashboard.impl.FriendLoanSummary;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import java.util.List;

/** Service responsible for computing friend-loan summaries from cash transactions. */
@FunctionalInterface
public interface FriendLoanSummaryService {

    /**
     * Computes one summary for each friend loan found in the provided transactions.
     *
     * @param transactions the cash transactions to inspect
     * @return the computed friend-loan summaries
     */
    List<FriendLoanSummary> summarize(List<CashTransaction> transactions);
}
