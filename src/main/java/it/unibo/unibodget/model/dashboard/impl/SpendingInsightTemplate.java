package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Concrete {@link AbstractMonthlyInsightTemplate} for spending comparisons.
 */
public final class SpendingInsightTemplate extends AbstractMonthlyInsightTemplate {

    private static final String SPENDING = "Spending";
    private static final String SPENT_PREFIX = "You spent ";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTitle() {
        return SPENDING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InsightTrend getIncreaseTrend() {
        return InsightTrend.NEGATIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InsightTrend getDecreaseTrend() {
        return InsightTrend.POSITIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<WalletInsight> handleZeroBaseline(final BigDecimal current, final BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0 && current.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.of(new WalletInsight(
                    SPENDING,
                    "No spending recorded this month or last month.",
                    InsightTrend.NEUTRAL,
                    BigDecimal.ZERO,
                    null
            ));
        }
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.of(new WalletInsight(
                    SPENDING,
                    SPENT_PREFIX + current + " this month; no spending was recorded last month.",
                    InsightTrend.NEUTRAL,
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
        return SPENT_PREFIX + percentage + "% more than last month (" + absoluteDelta + " more).";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String formatDecrease(final BigDecimal absoluteDelta, final BigDecimal percentage) {
        return SPENT_PREFIX + percentage + "% less than last month (" + absoluteDelta + " less).";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String formatUnchanged() {
        return "Your spending is in line with last month.";
    }
}
