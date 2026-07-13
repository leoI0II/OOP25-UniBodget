package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

/**
 * Serializer for complex JSON objects using Jackson.
 * <p>
 * This implementation delegates all serialization work to the
 * shared ObjectMapper configured in {@link PersistenceJacksonConfig}.
 * <p>
 */
public final class JsonComplexDataSerializer<T> implements DataSerializer<T> {

    @Override
    public String serialize(T value) throws DataSerializerException {
        try {
            // Serialize using the globally configured ObjectMapper
            return PersistenceJacksonConfig.mapper().writeValueAsString(value);
        } catch (Exception e) {
            // Wrap any Jackson exception into a domain-specific serializer exception
            throw new DataSerializerException("Jackson serialization failed", e);
        }
    }

}
