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
 * <p>
 * This deserializer converts a JSON string representing a UUID into an
 * {@code Optional<UUID>}. Empty or blank values are mapped to
 * {@code Optional.empty()}, ensuring safe handling of optional fields in the
 * persistence layer.</p>
 *
 * <p>
 * The expected input format is the canonical UUID string representation
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
    public Optional<UUID> deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException {

        final String raw = parser.getValueAsString();
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(raw));
    }

    /**
     * Called by Jackson when the JSON token is literally {@code null} —
     * in that case {@link #deserialize} is never invoked. Without this
     * override, a JSON null would deserialize to a Java {@code null}
     * instead of {@code Optional.empty()}.
     */
    @Override
    public Optional<UUID> getNullValue(final DeserializationContext ctxt) {
        return Optional.empty();
    }

}
