package it.unibo.unibodget.model.investment;

import it.unibo.unibodget.model.wallet.InvestmentAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BalanceSnapshot(
        LocalDateTime timestamp,
        BigDecimal totalPL,
        BigDecimal costBasis
) {
    /**
     * Builds a {@link BalanceSnapshot} for the given account at the current instant.
     *
     * @param account the account to snapshot
     * @return a new snapshot with current profit/loss and cost basis
     */
    public static BalanceSnapshot buildInvestmentSnapshot(final InvestmentAccount account) {
        return new BalanceSnapshot(
                LocalDateTime.now(),
                account.getTotalProfitLoss().amount(),
                account.getTotalCostBasis().amount()
        );
    }
}
