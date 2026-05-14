package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.dashboard.api.FriendLoanSummaryService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Default implementation of {@link FriendLoanSummaryService}.
 *
 * <p>This implementation derives friend-loan summaries directly from the
 * cash transactions currently stored in the wallet.</p>
 */
public final class DefaultFriendLoanSummaryService implements FriendLoanSummaryService {

    @Override
    public List<FriendLoanSummary> summarize(final List<CashTransaction> transactions) {
        Objects.requireNonNull(transactions);

        final Map<UUID, List<CashTransaction>> transactionsByLoanId = transactions.stream()
                .filter(CashTransaction::isFriendLoanTransaction)
                .filter(t -> t.getCategory().getType() == CategoryType.FRIEND_LOAN)
                .filter(t -> t.getFriendLoanId().isPresent())
                .collect(Collectors.groupingBy(t -> t.getFriendLoanId().orElseThrow()));

        return transactionsByLoanId.entrySet().stream()
                .map(entry -> buildSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(FriendLoanSummary::getNetBalance).reversed())
                .toList();
    }

    /**
     * Builds the summary corresponding to a single friend loan.
     *
     * @param friendLoanId
     *            the loan identifier
     * @param transactions
     *            the transactions linked to that loan
     * @return the computed summary
     */
    private FriendLoanSummary buildSummary(
            final UUID friendLoanId,
            final List<CashTransaction> transactions) {

        BigDecimal totalGiven = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;

        final String friendName = transactions.stream()
                .map(CashTransaction::getFriendName)
                .flatMap(Optional::stream)
                .findFirst()
                .orElse("Unknown friend");

        for (final CashTransaction transaction : transactions) {
            final BigDecimal amount = transaction.getAsset().amount();

            if (amount.signum() < 0) {
                totalGiven = totalGiven.add(amount.abs());
            } else if (amount.signum() > 0) {
                totalReceived = totalReceived.add(amount);
            }
        }

        final BigDecimal netBalance = totalGiven.subtract(totalReceived);

        return new FriendLoanSummary(
                friendLoanId,
                friendName,
                totalGiven,
                totalReceived,
                netBalance
        );
    }
}