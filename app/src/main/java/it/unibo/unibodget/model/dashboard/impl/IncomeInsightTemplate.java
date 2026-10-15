package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Concrete {@link MonthlyInsightTemplate} for income comparisons.
 */
public final class IncomeInsightTemplate extends MonthlyInsightTemplate {

    private static final String INCOME = "Income";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTitle() {
        return INCOME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InsightTrend getIncreaseTrend() {
        return InsightTrend.POSITIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InsightTrend getDecreaseTrend() {
        return InsightTrend.NEGATIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<WalletInsight> handleZeroBaseline(final BigDecimal current, final BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0 && current.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.of(new WalletInsight(
                    INCOME,
                    "No income recorded this month or last month.",
                    InsightTrend.NEUTRAL,
                    BigDecimal.ZERO,
                    null
            ));
        }
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.of(new WalletInsight(
                    INCOME,
                    "You recorded " + current + " income this month; no income was recorded last month.",
                    InsightTrend.POSITIVE,
                    current,
                    null
            ));
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String formatIncrease(final BigDecimal absoluteDelta, final BigDecimal percentage) {
        return "Your income increased by " + percentage + "% compared to last month.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String formatDecrease(final BigDecimal absoluteDelta, final BigDecimal percentage) {
        return "Your income decreased by " + percentage + "% compared to last month.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String formatUnchanged() {
        return "Your income matches last month.";
    }
}
