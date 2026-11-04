package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

import it.unibo.unibodget.model.dashboard.api.BudgetAlertStrategy;
import it.unibo.unibodget.model.dashboard.api.BudgetSettings;

/**
 * Base abstract class for budget alert strategies that evaluate limit ratios.
 */
public abstract class AbstractBudgetAlertStrategy implements BudgetAlertStrategy {

    private static final int SCALE = 8;

    /**
     * Computes the ratio between the current value and the limit value, if positive.
     *
     * @param currentValue the current spending
     * @param settings the budget settings
     * @return an {@link Optional} containing the ratio, or empty if limit &lt;= 0
     */
    protected final Optional<BigDecimal> computeRatio(
            final BigDecimal currentValue,
            final BudgetSettings settings) {
        Objects.requireNonNull(currentValue, "currentValue must not be null");
        Objects.requireNonNull(settings, "settings must not be null");
        final BigDecimal limit = settings.getLimitValue();
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }
        return Optional.of(currentValue.divide(limit, SCALE, RoundingMode.HALF_UP));
    }
}
