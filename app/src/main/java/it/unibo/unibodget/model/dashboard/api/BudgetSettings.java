package it.unibo.unibodget.model.dashboard.api;

/**
 * Represents the budget configuration defined by the user.
 * <p>
 * This abstraction is used to avoid representing the budget as a raw primitive
 * value, making its meaning explicit in the model and leaving room for future
 * extensions, such as different thresholds or temporal scopes.
 */
public interface BudgetSettings {

    /**
     * Returns the configured budget limit.
     * <p>
     * This is the value against which the current aggregated dashboard amount
     * is compared.
     *
     * @return the configured budget limit
     */
    double getLimitValue();

    /**
     * Returns the warning threshold.
     * <p>
     * The threshold is expressed as a ratio in the range {@code [0, 1]}.
     * For example, a value of {@code 0.8} means that the warning state begins
     * when the current value reaches 80% of the configured limit.
     *
     * @return the warning threshold ratio
     */
    double getWarningThreshold();
}
