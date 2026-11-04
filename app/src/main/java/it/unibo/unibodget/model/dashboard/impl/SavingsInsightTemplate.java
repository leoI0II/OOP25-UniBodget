package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;

/**
 * Concrete {@link AbstractMonthlyInsightTemplate} for savings comparisons.
 */
public final class SavingsInsightTemplate extends AbstractMonthlyInsightTemplate {

    private static final String SAVINGS = "Savings";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTitle() {
        return SAVINGS;
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
    protected String formatIncrease(final BigDecimal absoluteDelta, final BigDecimal percentage) {
        return "You saved " + absoluteDelta + " more than last month.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String formatDecrease(final BigDecimal absoluteDelta, final BigDecimal percentage) {
        return "You saved " + absoluteDelta + " less than last month.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String formatUnchanged() {
        return "Your savings match last month.";
    }
}
