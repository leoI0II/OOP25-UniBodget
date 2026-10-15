package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;

/**
 * Strategy representing an approaching-limit budget condition.
 * Evaluates whether current expenditure has reached the warning threshold ratio.
 */
public final class WarningBudgetStrategy extends AbstractBudgetAlertStrategy {

    private static final String DEFAULT_THRESHOLD_STR = "0.80";
    private static final BigDecimal DEFAULT_WARNING_THRESHOLD = new BigDecimal(DEFAULT_THRESHOLD_STR);

    private final BigDecimal fallbackThreshold;

    /**
     * Creates a warning budget strategy with the default 80% threshold.
     */
    public WarningBudgetStrategy() {
        this(DEFAULT_WARNING_THRESHOLD);
    }

    /**
     * Creates a warning budget strategy with a custom fallback warning threshold ratio.
     *
     * @param fallbackThreshold the threshold ratio used when settings does not specify one
     */
    public WarningBudgetStrategy(final BigDecimal fallbackThreshold) {
        this.fallbackThreshold = Objects.requireNonNull(fallbackThreshold, "fallbackThreshold must not be null");
    }

    @Override
    public boolean applies(final BigDecimal currentValue, final BudgetSettings settings) {
        return computeRatio(currentValue, settings)
                .map(ratio -> {
                    final BigDecimal threshold = settings.getWarningThreshold() != null
                            ? settings.getWarningThreshold()
                            : fallbackThreshold;
                    return ratio.compareTo(threshold) >= 0;
                })
                .orElse(false);
    }

    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BudgetSettings settings) {
        return BudgetStatus.WARNING;
    }

    @Override
    public BudgetStatus evaluate(final BigDecimal currentValue, final BigDecimal limitValue) {
        return BudgetStatus.WARNING;
    }
}
