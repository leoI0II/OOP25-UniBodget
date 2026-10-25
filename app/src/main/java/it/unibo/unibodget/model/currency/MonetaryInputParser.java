package it.unibo.unibodget.model.currency;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Utility class for normalizing, parsing, and validating monetary and numeric inputs across the
 * application.
 *
 * <p>Supports both comma and period decimal delimiters, strips formatting whitespace and grouping
 * apostrophes, and provides fine-grained validation predicates for non-negativity, strict
 * positivity, and maximum decimal scale.
 */
public final class MonetaryInputParser {

    private MonetaryInputParser() {
        // utility class
    }

    /**
     * Normalizes a raw numeric string by trimming whitespace, removing grouping separators, and
     * converting comma decimal separators to periods.
     *
     * @param raw the raw input string
     * @return the sanitized string, or {@code null} if the input is null or blank
     */
    public static String sanitize(final String raw) {
        if (raw == null) {
            return null;
        }
        final String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replace("'", "")
                .replace(" ", "")
                .replace(',', '.');
    }

    /**
     * Parses a monetary string into a {@link BigDecimal}.
     *
     * @param raw the input string
     * @return an {@link Optional} containing the parsed value, or empty if parsing fails
     */
    public static Optional<BigDecimal> parse(final String raw) {
        final String sanitized = sanitize(raw);
        if (sanitized == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(sanitized));
        } catch (final NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses a monetary string, ensuring the resulting value is strictly positive (&gt; 0).
     *
     * @param raw the input string
     * @return an {@link Optional} containing the positive value, or empty if invalid or non-positive
     */
    public static Optional<BigDecimal> parsePositive(final String raw) {
        return parse(raw).filter(val -> val.compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Parses a monetary string, ensuring the resulting value is non-negative (&ge; 0).
     *
     * @param raw the input string
     * @return an {@link Optional} containing the non-negative value, or empty if invalid or negative
     */
    public static Optional<BigDecimal> parseNonNegative(final String raw) {
        return parse(raw).filter(val -> val.compareTo(BigDecimal.ZERO) >= 0);
    }

    /**
     * Parses a strictly positive monetary string and validates that its fractional scale does not
     * exceed the specified maximum.
     *
     * @param raw the input string
     * @param maxScale the maximum allowed number of decimal places
     * @return an {@link Optional} containing the value, or empty if invalid or exceeding max scale
     */
    public static Optional<BigDecimal> parsePositiveWithMaxScale(final String raw, final int maxScale) {
        return parsePositive(raw).filter(val -> val.scale() <= maxScale);
    }

    /**
     * Parses a numeric string into a {@link Double}, tolerating both commas and periods.
     *
     * @param raw the input string
     * @return an {@link Optional} containing the double, or empty if parsing fails or input is NaN
     */
    public static Optional<Double> parseDouble(final String raw) {
        final String sanitized = sanitize(raw);
        if (sanitized == null) {
            return Optional.empty();
        }
        try {
            final double val = Double.parseDouble(sanitized);
            if (Double.isNaN(val) || Double.isInfinite(val)) {
                return Optional.empty();
            }
            return Optional.of(val);
        } catch (final NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses a strictly positive numeric string into a {@link Double}.
     *
     * @param raw the input string
     * @return an {@link Optional} containing the positive double, or empty if invalid
     */
    public static Optional<Double> parsePositiveDouble(final String raw) {
        return parseDouble(raw).filter(val -> val > 0.0);
    }

    /**
     * Parses a non-negative numeric string into a {@link Double}.
     *
     * @param raw the input string
     * @return an {@link Optional} containing the non-negative double, or empty if invalid
     */
    public static Optional<Double> parseNonNegativeDouble(final String raw) {
        return parseDouble(raw).filter(val -> val >= 0.0);
    }

}
