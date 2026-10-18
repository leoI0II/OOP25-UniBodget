package it.unibo.unibodget.model.dashboard.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Tests the collections exposed by {@link DefaultDashboardSnapshot}.
 */
final class DefaultDashboardSnapshotTest {

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
                FiatCurrency.EUR.getCode(),
                new BigDecimal("1700.00"),
                List.of(),
                Map.of("Food", new BigDecimal("300.00")),
                new BigDecimal("1000.00"),
                new BigDecimal("0.80"),
                BudgetStatus.SAFE,
                List.of(summary),
                List.of(insight),
                List.of()
        );

        assertEquals(1, snapshot.getFriendLoanSummaries().size());
        assertEquals(summary, snapshot.getFriendLoanSummaries().get(0));

        assertEquals(1, snapshot.getWalletInsights().size());
        assertEquals(insight, snapshot.getWalletInsights().get(0));

        assertTrue(snapshot.getAllWallets().isEmpty());

        final List<FriendLoanSummary> summaries = snapshot.getFriendLoanSummaries();
        final List<WalletInsight> insights = snapshot.getWalletInsights();
        final List<CashAccount> wallets = snapshot.getAllWallets();
        final CashAccount extraWallet = new CashAccount("Extra", FiatCurrency.EUR);

        assertThrows(
                UnsupportedOperationException.class,
                () -> summaries.add(summary)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> insights.add(insight)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> wallets.add(extraWallet)
        );
    }
}
