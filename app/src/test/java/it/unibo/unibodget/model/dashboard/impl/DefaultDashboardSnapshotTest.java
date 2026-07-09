package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Tests for {@link DefaultDashboardSnapshot}.
 */
class DefaultDashboardSnapshotTest {

    @Test
    void shouldExposeFriendLoanSummariesAndWalletInsightsAndKeepCollectionsImmutable() {
        final FriendLoanSummary summary = new FriendLoanSummary(
                UUID.randomUUID(),
                "Marco",
                new BigDecimal("100.00"),
                new BigDecimal("30.00"),
                new BigDecimal("70.00")
        );

        final WalletInsight insight = new WalletInsight(
                "Spending",
                "You spent less than last month.",
                InsightTrend.POSITIVE,
                new BigDecimal("20.00"),
                new BigDecimal("10.00")
        );

        final DefaultDashboardSnapshot snapshot = new DefaultDashboardSnapshot(
                "Main wallet",
                "EUR",
                new BigDecimal("1700.00"),
                List.<Transaction>of(),
                Map.of("Food", new BigDecimal("300.00")),
                new BigDecimal("1000.00"),
                new BigDecimal("0.80"),
                BudgetStatus.SAFE,
                List.of(summary),
                List.of(insight)
        );

        assertEquals(1, snapshot.getFriendLoanSummaries().size());
        assertEquals("Marco", snapshot.getFriendLoanSummaries().get(0).getFriendName());

        assertEquals(1, snapshot.getWalletInsights().size());
        assertEquals("Spending", snapshot.getWalletInsights().get(0).title());

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.getFriendLoanSummaries().add(summary)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.getWalletInsights().add(insight)
        );
    }
}