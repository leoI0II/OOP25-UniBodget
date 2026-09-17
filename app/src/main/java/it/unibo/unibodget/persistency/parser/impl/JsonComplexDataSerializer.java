package it.unibo.unibodget.persistency.parser.impl;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

/**
 * Serializer for complex JSON objects using Jackson.
 * 
 * <p>
 * This implementation delegates all serialization work to the
 * shared ObjectMapper configured in {@link PersistenceJacksonConfig}.
 * </p>
 * 
 * @param <T> the type of object to serialize
 */
public final class JsonComplexDataSerializer<T> implements DataSerializer<T> {

    /**
     * Serializes the given value into a JSON string using the configured
     * Jackson {@code ObjectMapper}.
     *
     * @param value the object to serialize; must not be {@code null}
     * @return the JSON representation of the given value
     * @throws DataSerializerException if Jackson fails to serialize the object
     */
    @Override
    public String serialize(final T value) throws DataSerializerException {
        try {
            // Serialize using the globally configured ObjectMapper
            return PersistenceJacksonConfig.mapper().writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            // Wrap any Jackson exception into a domain-specific serializer exception
            throw new DataSerializerException("Jackson serialization failed", e);
        }
    }

}
