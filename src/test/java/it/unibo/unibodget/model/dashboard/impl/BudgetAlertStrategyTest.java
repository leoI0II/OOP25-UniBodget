package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;
import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Unit tests for {@link BudgetAlertStrategy} implementations and polymorphic evaluation.
 */
class BudgetAlertStrategyTest {

    private static final String LIMIT_1000 = "1000.00";
    private static final String THRESHOLD_80 = "0.80";

    @Test
    void shouldEvaluateCriticalStrategy() {
        final BudgetAlertStrategy critical = new CriticalBudgetStrategy();
        final BudgetSettings settings = new DefaultBudgetSettings(
                new BigDecimal(LIMIT_1000),
                new BigDecimal(THRESHOLD_80)
        );

        assertTrue(critical.applies(new BigDecimal(LIMIT_1000), settings));
        assertTrue(critical.applies(new BigDecimal("1200.00"), settings));
        assertFalse(critical.applies(new BigDecimal("999.99"), settings));
        assertFalse(critical.applies(new BigDecimal("500.00"), settings));
        assertEquals(BudgetStatus.CRITICAL, critical.evaluate(new BigDecimal(LIMIT_1000), settings));
    }

    @Test
    void shouldEvaluateWarningStrategy() {
        final BudgetAlertStrategy warning = new WarningBudgetStrategy();
        final BudgetSettings settings = new DefaultBudgetSettings(
                new BigDecimal(LIMIT_1000),
                new BigDecimal(THRESHOLD_80)
        );

        assertTrue(warning.applies(new BigDecimal("800.00"), settings));
        assertTrue(warning.applies(new BigDecimal("950.00"), settings));
        assertFalse(warning.applies(new BigDecimal("799.99"), settings));
        assertFalse(warning.applies(new BigDecimal("300.00"), settings));
        assertEquals(BudgetStatus.WARNING, warning.evaluate(new BigDecimal("800.00"), settings));
    }

    @Test
    void shouldEvaluateSafeStrategy() {
        final BudgetAlertStrategy safe = new SafeBudgetStrategy();
        final BudgetSettings settings = new DefaultBudgetSettings(
                new BigDecimal(LIMIT_1000),
                new BigDecimal(THRESHOLD_80)
        );

        assertTrue(safe.applies(new BigDecimal("100.00"), settings));
        assertTrue(safe.applies(new BigDecimal("0.00"), settings));
        assertEquals(BudgetStatus.SAFE, safe.evaluate(new BigDecimal("100.00"), settings));
    }

    @Test
    void shouldSupportCustomStrategiesInMonitor() {
        final BudgetAlertStrategy customCritical = new CriticalBudgetStrategy(new BigDecimal("0.90"));
        final DefaultBudgetMonitor monitor = new DefaultBudgetMonitor(
                List.of(customCritical, new SafeBudgetStrategy())
        );

        final BudgetSettings settings = new DefaultBudgetSettings(
                new BigDecimal(LIMIT_1000),
                new BigDecimal(THRESHOLD_80)
        );

        assertEquals(BudgetStatus.CRITICAL, monitor.getBudgetStatus(new BigDecimal("920.00"), settings));
        assertEquals(BudgetStatus.SAFE, monitor.getBudgetStatus(new BigDecimal("850.00"), settings));
    }
}
