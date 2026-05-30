package it.unibo.unibodget.model.investment;

import it.unibo.unibodget.model.wallet.InvestmentAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a point-in-time snapshot of an investment account's financial state.
 *
 * @param timestamp the date and time when the snapshot was taken
 * @param totalPL   the total profit or loss at the time of the snapshot
 * @param costBasis the total cost basis at the time of the snapshot
 */
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
