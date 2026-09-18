package it.unibo.unibodget.view.dashboard.impl;

import java.math.BigDecimal;

/**
 * Immutable dialog result representing the raw user input required to update
 * the monthly budget of the currently selected wallet.
 *
 * @param limitValue
 *            the monthly budget limit
 * @param warningThreshold
 *            the warning threshold ratio in the range [0, 1]
 */
public record EditBudgetRequest(
        BigDecimal limitValue,
        BigDecimal warningThreshold) {
}