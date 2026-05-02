package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;

/**
 * Base class for all data parsers. Provides common utility methods
 * used by concrete parser implementations.
 *
 * @param <T> the type of object produced by the parser
 */
public abstract class BasicDataParser<T> implements DataParser<T> {

    /**
     * Trims the given string, returning {@code null} if the input is {@code null}.
     *
     * @param s the string to trim
     * @return the trimmed string, or {@code null} if the input was {@code null}
     */
    protected String trim(String s) {
        return s == null ? null : s.trim();
    }

    /**
     * Ensures that a condition is true. If the condition is false,
     * a {@link DataParserException} is thrown.
     *
     * @param condition the condition to check
     * @param message   the exception message if the condition is false
     * @throws DataParserException if the condition is false
     */
    protected void ensure(boolean condition, String message) throws DataParserException {
        if (!condition) {
            throw new DataParserException(message);
        }
    }

    /**
     * Extracts the value of a field from a JSON string.
     *
     * @param json the JSON string
     * @param field the field name
     * @return the value of the field
     * @throws DataParserException if the field is missing
     */
    protected String extractField(String json, String field) throws DataParserException {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) {
            throw new DataParserException("Missing field: " + field);
        }
        int colon = json.indexOf(":", idx);
        int start = json.indexOf("\"", colon + 1) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
