package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract base class implementing the Template Method Pattern for monthly comparison insights.
 *
 * <p>
 * This class outlines the invariant algorithm for comparing financial figures between the
 * current and previous month, calculating deltas, absolute differences, and percentage variations.
 * Primitive operations and hooks are deferred to subclasses to provide metric-specific formatting,
 * titles, and trend classifications.
 * </p>
 */
public abstract class AbstractMonthlyInsightTemplate {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int PERCENT_SCALE = 2;

    /**
     * Template method that produces a {@link WalletInsight} comparing current and previous values.
     *
     * @param current
     *            the value for the current month, must not be null
     * @param previous
     *            the value for the previous month, must not be null
     * @return the computed wallet insight
     */
    public final WalletInsight generate(final BigDecimal current, final BigDecimal previous) {
        Objects.requireNonNull(current, "current must not be null");
        Objects.requireNonNull(previous, "previous must not be null");

        final Optional<WalletInsight> zeroBaseline = handleZeroBaseline(current, previous);
        if (zeroBaseline.isPresent()) {
            return zeroBaseline.get();
        }

        final BigDecimal delta = current.subtract(previous);
        final BigDecimal absoluteDelta = delta.abs();
        final BigDecimal percentage = computePercentageChange(previous.abs(), absoluteDelta);

        final int comparison = delta.compareTo(BigDecimal.ZERO);
        if (comparison > 0) {
            return new WalletInsight(
                    getTitle(),
                    formatIncrease(absoluteDelta, percentage),
                    getIncreaseTrend(),
                    absoluteDelta,
                    percentage
            );
        }
        if (comparison < 0) {
            return new WalletInsight(
                    getTitle(),
                    formatDecrease(absoluteDelta, percentage),
                    getDecreaseTrend(),
                    absoluteDelta,
                    percentage
            );
        }

        return new WalletInsight(
                getTitle(),
                formatUnchanged(),
                InsightTrend.NEUTRAL,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    /**
     * Hook method to handle zero baselines (such as both values being zero, or previous month having no data).
     *
     * @param current
     *            the current month value
     * @param previous
     *            the previous month value
     * @return an {@link Optional} containing the insight if special handling was triggered, otherwise empty
     */
    protected Optional<WalletInsight> handleZeroBaseline(final BigDecimal current, final BigDecimal previous) {
        return Optional.empty();
    }

    /**
     * Returns the title of the insight.
     *
     * @return the insight title
     */
    protected abstract String getTitle();

    /**
     * Returns the trend associated with an increase in the metric.
     *
     * @return the increase trend
     */
    protected abstract InsightTrend getIncreaseTrend();

    /**
     * Returns the trend associated with a decrease in the metric.
     *
     * @return the decrease trend
     */
    protected abstract InsightTrend getDecreaseTrend();

    /**
     * Formats the message when the metric has increased.
     *
     * @param absoluteDelta
     *            the absolute difference
     * @param percentage
     *            the percentage change, or {@code null} if previous base was zero
     * @return the formatted increase message
     */
    protected abstract String formatIncrease(BigDecimal absoluteDelta, BigDecimal percentage);

    /**
     * Formats the message when the metric has decreased.
     *
     * @param absoluteDelta
     *            the absolute difference
     * @param percentage
     *            the percentage change, or {@code null} if previous base was zero
     * @return the formatted decrease message
     */
    protected abstract String formatDecrease(BigDecimal absoluteDelta, BigDecimal percentage);

    /**
     * Formats the message when the metric is unchanged.
     *
     * @return the unchanged message
     */
    protected abstract String formatUnchanged();

    /**
     * Computes the percentage change between a base value and an absolute change.
     *
     * @param base
     *            the base value
     * @param delta
     *            the absolute change
     * @return the percentage change, or {@code null} if base is null or zero
     */
    protected BigDecimal computePercentageChange(final BigDecimal base, final BigDecimal delta) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return delta.multiply(ONE_HUNDRED).divide(base, PERCENT_SCALE, RoundingMode.HALF_UP);
    }
}
