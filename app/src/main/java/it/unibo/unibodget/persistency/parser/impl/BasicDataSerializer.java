package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

/**
 * Base class providing common utility methods for serializer implementations.
 * 
 * <p>
 * This class is intended to be extended by concrete serializers that convert
 * Java objects into a serialized representation
 * </p>
 *
 * @param <T> the type of objects handled by the serializer
 */
public class BasicDataSerializer<T> {

    /**
     * Validates a condition and throws a {@link DataSerializerException}
     * if the condition is false.
     *
     * @param condition the boolean condition to validate
     * @param message the error message used if validation fails
     * @throws DataSerializerException if {@code condition} is {@code false}
     */
    protected void ensure(final boolean condition, final String message) throws DataSerializerException {
        if (!condition) {
            throw new DataSerializerException(message);
        }
    }

    /**
     * Wraps the given string in double quotes.
     *
     * @param s the string to quote
     * @return the quoted string, e.g. {@code "value"}
     */
    protected String quote(final String s) {
        return "\"" + s + "\"";
    }

}
