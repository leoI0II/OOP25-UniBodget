package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.BudgetSettings;

/**
 * Default immutable implementation of {@link BudgetSettings}.
 *
 * <p>
 * This class stores the budget configuration chosen by the user and validates
 * the provided values at construction time.</p>
 */
public final class DefaultBudgetSettings implements BudgetSettings {

    private static final BigDecimal DEFAULT_WARNING_THRESHOLD = BigDecimal.valueOf(0.8);

    private final BigDecimal limitValue;
    private final BigDecimal warningThreshold;

    /**
     * Creates a new budget configuration with the provided limit and the
     * default warning threshold.
     *
     * @param limitValue the budget limit configured by the user
     */
    public DefaultBudgetSettings(final BigDecimal limitValue) {
        this(limitValue, DEFAULT_WARNING_THRESHOLD);
    }

    /**
     * Creates a new budget configuration with the provided limit and warning
     * threshold.
     *
     * @param limitValue the budget limit configured by the user
     * @param warningThreshold the ratio at which the warning state begins
     * @throws IllegalArgumentException if the limit is negative or if the
     * warning threshold is outside {@code [0, 1]}
     */
    public DefaultBudgetSettings(final BigDecimal limitValue, final BigDecimal warningThreshold) {
        this.limitValue = Objects.requireNonNull(limitValue);
        if (limitValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The budget limit cannot be negative");
        }
        if (warningThreshold.compareTo(BigDecimal.ZERO) < 0
                || warningThreshold.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("The warning threshold must be between 0 and 1");
        }
        this.warningThreshold = warningThreshold;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getLimitValue() {
        return limitValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getWarningThreshold() {
        return warningThreshold;
    }
}
