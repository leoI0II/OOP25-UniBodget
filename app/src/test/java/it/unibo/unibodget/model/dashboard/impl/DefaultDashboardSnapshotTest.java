package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.transactions.base.AbstractTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Tests for {@link DefaultDashboardSnapshot}.
 */
class DefaultDashboardSnapshotTest {

    private static final String FOOD = "Food";
    private static final String S_300 = "300.00";
    private static final String S_1700 = "1700.00";
    private static final String S_1000 = "1000.00";
    private static final String S_0_80 = "0.80";
    private static final String MAIN_WALLET = "Main wallet";
    private static final String EUR = "EUR";

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
                MAIN_WALLET,
                EUR,
                new BigDecimal(S_1700),
                List.<AbstractTransaction>of(),
                Map.of(FOOD, new BigDecimal(S_300)),
                new BigDecimal(S_1000),
                new BigDecimal(S_0_80),
                BudgetStatus.SAFE,
                List.of(summary),
                List.of(insight),
                List.<CashAccount>of()
        );

        assertEquals(1, snapshot.getFriendLoanSummaries().size());
        assertEquals("Marco", snapshot.getFriendLoanSummaries().get(0).getFriendName());

        assertEquals(1, snapshot.getWalletInsights().size());
        assertEquals("Spending", snapshot.getWalletInsights().get(0).title());

        assertEquals(0, snapshot.getAllWallets().size());
        assertEquals(BigDecimal.ZERO, snapshot.getCurrentMonthBudgetUsage());

        final DefaultDashboardSnapshot snapshotWithUsage = new DefaultDashboardSnapshot(
                MAIN_WALLET,
                EUR,
                new BigDecimal(S_1700),
                List.<AbstractTransaction>of(),
                Map.of(FOOD, new BigDecimal(S_300)),
                Map.of(FOOD, new BigDecimal(S_300)),
                new BigDecimal(S_1000),
                new BigDecimal(S_0_80),
                BudgetStatus.SAFE,
                List.of(summary),
                List.of(insight),
                List.<CashAccount>of(),
                new BigDecimal("250.00")
        );
        assertEquals(new BigDecimal("250.00"), snapshotWithUsage.getCurrentMonthBudgetUsage());

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.getFriendLoanSummaries().add(summary)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.getWalletInsights().add(insight)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.getAllWallets().add(new CashAccount("Extra", FiatCurrency.EUR))
        );
    }
}
