package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable dashboard insight ready to be displayed in the UI.
 *
 * <p>A wallet insight contains:
 * <ul>
 *   <li>a short title identifying the type of insight,</li>
 *   <li>a user-facing message,</li>
 *   <li>a semantic trend used for presentation,</li>
 *   <li>the absolute change in amount,</li>
 *   <li>an optional percentage change.</li>
 * </ul>
 * </p>
 *
 * @param title
 *            the short title of the insight
 * @param message
 *            the user-facing message describing the insight
 * @param trend
 *            the semantic trend associated with the insight
 * @param deltaAmount
 *            the absolute monetary change represented by the insight
 * @param deltaPercentage
 *            the percentage change represented by the insight, or {@code null}
 *            when not applicable
 */
public record WalletInsight(
        String title,
        String message,
        InsightTrend trend,
        BigDecimal deltaAmount,
        BigDecimal deltaPercentage) {

    /**
     * Creates a new wallet insight.
     *
     * @throws NullPointerException
     *             if title, message, trend, or deltaAmount is null
     */
    public WalletInsight {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(trend, "trend must not be null");
        Objects.requireNonNull(deltaAmount, "deltaAmount must not be null");
    }
}