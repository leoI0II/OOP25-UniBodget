package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;

/**
 * Parser for complex JSON objects using Jackson.
 * <p>
 * This parser supports:
 * <ul>
 *     <li>polymorphic deserialization (via Jackson mixins)</li>
 *     <li>ignoring unknown fields</li>
 *     <li>ISO date formats</li>
 * </ul>
 * thanks to the configuration provided by {@link PersistenceJacksonConfig}.
 *
 * @param <T> the target type to deserialize into
 */
public final class JsonComplexDataParser<T> implements DataParser<T> {

    /** The class Jackson should deserialize the JSON into. */
    private final Class<T> targetClass;

    /**
     * Creates a new parser for the given target type.
     *
     * @param targetClass the class representing the JSON root object
     */
    public JsonComplexDataParser(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public T parse(String json) throws DataParserException {
        try {
            // Deserialize using the globally configured ObjectMapper
            return PersistenceJacksonConfig.mapper().readValue(json, targetClass);
        } catch (Exception e) {
            // Wrap Jackson exceptions into a domain-specific parser exception
            throw new DataParserException("Jackson complex parsing failed: " + e.getMessage(), e);
        }
    }
}
