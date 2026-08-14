package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Tests for {@link DefaultBudgetMonitor}.
 */
class DefaultBudgetMonitorTest {

    private static final String S_1000 = "1000.00";
    private static final String S_0_80 = "0.80";

    private final BudgetMonitor monitor = new DefaultBudgetMonitor();

    @Test
    void shouldReturnSafeWhenCurrentValueIsBelowWarningThreshold() {
        final DefaultBudgetSettings settings =
                new DefaultBudgetSettings(new BigDecimal(S_1000), new BigDecimal(S_0_80));

        final BudgetStatus result = monitor.getBudgetStatus(new BigDecimal("500.00"), settings);

        assertEquals(BudgetStatus.SAFE, result);
    }

    @Test
    void shouldReturnWarningWhenCurrentValueReachesWarningThreshold() {
        final DefaultBudgetSettings settings =
                new DefaultBudgetSettings(new BigDecimal(S_1000), new BigDecimal(S_0_80));

        final BudgetStatus result = monitor.getBudgetStatus(new BigDecimal("800.00"), settings);

        assertEquals(BudgetStatus.WARNING, result);
    }

    @Test
    void shouldReturnCriticalWhenCurrentValueReachesLimit() {
        final DefaultBudgetSettings settings =
                new DefaultBudgetSettings(new BigDecimal(S_1000), new BigDecimal(S_0_80));

        final BudgetStatus result = monitor.getBudgetStatus(new BigDecimal(S_1000), settings);

        assertEquals(BudgetStatus.CRITICAL, result);
    }

    @Test
    void shouldReturnCriticalWhenCurrentValueExceedsLimit() {
        final DefaultBudgetSettings settings =
                new DefaultBudgetSettings(new BigDecimal(S_1000), new BigDecimal(S_0_80));

        final BudgetStatus result = monitor.getBudgetStatus(new BigDecimal("1200.00"), settings);

        assertEquals(BudgetStatus.CRITICAL, result);
    }

    @Test
    void shouldReturnSafeWhenLimitIsZero() {
        final DefaultBudgetSettings settings =
                new DefaultBudgetSettings(BigDecimal.ZERO, new BigDecimal(S_0_80));

        final BudgetStatus result = monitor.getBudgetStatus(new BigDecimal("100.00"), settings);

        assertEquals(BudgetStatus.SAFE, result);
    }
}
