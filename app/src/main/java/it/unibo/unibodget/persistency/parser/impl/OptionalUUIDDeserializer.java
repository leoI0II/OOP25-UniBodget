package it.unibo.unibodget.persistency.parser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Custom Jackson deserializer for {@link Optional Optional&lt;UUID&gt;}.
 *
 * <p>This deserializer converts a JSON string representing a UUID into an
 * {@code Optional<UUID>}. Empty or blank values are mapped to
 * {@code Optional.empty()}, ensuring safe handling of optional fields in the
 * persistence layer.</p>
 *
 * <p>The expected input format is the canonical UUID string representation
 * produced by {@link UUID#toString()}.</p>
 */
public final class OptionalUUIDDeserializer extends JsonDeserializer<Optional<UUID>> {

    /**
     * Deserializes a JSON string into an {@link Optional Optional&lt;UUID&gt;}.
     *
     * @param parser the JSON parser providing the string value
     * @param ctxt the deserialization context
     * @return an {@code Optional<UUID>} containing the parsed UUID, or empty if
     *         the input is null or blank
     * @throws IOException if the input cannot be read or parsed
     */
    @Override
    public Optional<UUID> deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException {

        String raw = parser.getValueAsString();
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(raw));
    }

}
