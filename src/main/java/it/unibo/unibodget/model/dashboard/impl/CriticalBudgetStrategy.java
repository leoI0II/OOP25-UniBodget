package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Strategy representing a critical budget condition.
 * Evaluates whether current expenditure has reached or exceeded the critical threshold (default 100%).
 */
public final class CriticalBudgetStrategy extends AbstractBudgetAlertStrategy {

    private static final BigDecimal DEFAULT_CRITICAL_THRESHOLD = BigDecimal.ONE;

    private final BigDecimal criticalThreshold;

    /**
     * Creates a critical budget strategy with the default 100% threshold.
     */
    public CriticalBudgetStrategy() {
        this(DEFAULT_CRITICAL_THRESHOLD);
    }

    /**
     * Creates a critical budget strategy with a custom threshold ratio.
     *
     * @param criticalThreshold the ratio above which budget is deemed critical
     */
    public CriticalBudgetStrategy(final BigDecimal criticalThreshold) {
        this.criticalThreshold = Objects.requireNonNull(criticalThreshold, "criticalThreshold must not be null");
    }

    @Override
    public boolean applies(final BigDecimal currentValue, final BudgetSettings settings) {
        return computeRatio(currentValue, settings)
                .map(ratio -> ratio.compareTo(criticalThreshold) >= 0)
                .orElse(false);
    }

    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BudgetSettings settings) {
        return BudgetStatus.CRITICAL;
    }

    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BigDecimal limitValue) {
        return BudgetStatus.CRITICAL;
    }
}
