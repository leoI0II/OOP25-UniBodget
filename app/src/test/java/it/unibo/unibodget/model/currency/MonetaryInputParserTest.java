package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MonetaryInputParser}.
 */
class MonetaryInputParserTest {

    private static final String VALUE_TEN = "10.00";
    private static final String VALUE_TEN_COMMA = "10,00";
    private static final String VALUE_ZERO = "0.00";
    private static final String VALUE_NEGATIVE = "-5.50";
    private static final String INVALID_TEXT = "invalid_text";
    private static final String SANITIZED_RESULT = "1234.56";
    private static final String VALUE_DOUBLE = "25,75";
    private static final double EXPECTED_DOUBLE = 25.75;

    @Test
    void shouldSanitizeStringProperly() {
        assertNull(MonetaryInputParser.sanitize(null));
        assertNull(MonetaryInputParser.sanitize(""));
        assertNull(MonetaryInputParser.sanitize("   "));

        assertEquals(SANITIZED_RESULT, MonetaryInputParser.sanitize("1234,56"));
        assertEquals(SANITIZED_RESULT, MonetaryInputParser.sanitize(" 1 234,56 "));
        assertEquals(SANITIZED_RESULT, MonetaryInputParser.sanitize("1'234.56"));
    }

    @Test
    void shouldParseValidBigDecimalWithDotOrComma() {
        final Optional<BigDecimal> withDot = MonetaryInputParser.parse(VALUE_TEN);
        assertTrue(withDot.isPresent());
        assertEquals(new BigDecimal(VALUE_TEN), withDot.get());

        final Optional<BigDecimal> withComma = MonetaryInputParser.parse(VALUE_TEN_COMMA);
        assertTrue(withComma.isPresent());
        assertEquals(new BigDecimal(VALUE_TEN), withComma.get());

        assertFalse(MonetaryInputParser.parse(INVALID_TEXT).isPresent());
        assertFalse(MonetaryInputParser.parse(null).isPresent());
    }

    @Test
    void shouldValidatePositiveAmounts() {
        assertTrue(MonetaryInputParser.parsePositive(VALUE_TEN).isPresent());
        assertTrue(MonetaryInputParser.parsePositive("0.01").isPresent());

        assertFalse(MonetaryInputParser.parsePositive(VALUE_ZERO).isPresent());
        assertFalse(MonetaryInputParser.parsePositive(VALUE_NEGATIVE).isPresent());
        assertFalse(MonetaryInputParser.parsePositive(INVALID_TEXT).isPresent());
    }

    @Test
    void shouldValidateNonNegativeAmounts() {
        assertTrue(MonetaryInputParser.parseNonNegative(VALUE_TEN).isPresent());
        assertTrue(MonetaryInputParser.parseNonNegative(VALUE_ZERO).isPresent());

        assertFalse(MonetaryInputParser.parseNonNegative(VALUE_NEGATIVE).isPresent());
        assertFalse(MonetaryInputParser.parseNonNegative(INVALID_TEXT).isPresent());
    }

    @Test
    void shouldValidateMaxScale() {
        final int scaleTwo = 2;
        assertTrue(MonetaryInputParser.parsePositiveWithMaxScale("12.3", scaleTwo).isPresent());
        assertTrue(MonetaryInputParser.parsePositiveWithMaxScale("12.34", scaleTwo).isPresent());

        assertFalse(MonetaryInputParser.parsePositiveWithMaxScale("12.345", scaleTwo).isPresent());
        assertFalse(MonetaryInputParser.parsePositiveWithMaxScale(VALUE_NEGATIVE, scaleTwo).isPresent());
    }

    @Test
    void shouldParseDoublesProperly() {
        final Optional<Double> parsed = MonetaryInputParser.parseDouble(VALUE_DOUBLE);
        assertTrue(parsed.isPresent());
        assertEquals(EXPECTED_DOUBLE, parsed.get());

        assertFalse(MonetaryInputParser.parseDouble(INVALID_TEXT).isPresent());
        assertFalse(MonetaryInputParser.parseDouble(null).isPresent());

        assertTrue(MonetaryInputParser.parsePositiveDouble("1.5").isPresent());
        assertFalse(MonetaryInputParser.parsePositiveDouble("-1.5").isPresent());
        assertFalse(MonetaryInputParser.parsePositiveDouble("0.0").isPresent());

        assertTrue(MonetaryInputParser.parseNonNegativeDouble("0.0").isPresent());
        assertTrue(MonetaryInputParser.parseNonNegativeDouble("1.5").isPresent());
        assertFalse(MonetaryInputParser.parseNonNegativeDouble("-1.5").isPresent());
    }
}
