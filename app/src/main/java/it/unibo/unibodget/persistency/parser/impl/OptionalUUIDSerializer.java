package it.unibo.unibodget.persistency.parser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Custom Jackson serializer for {@link Optional Optional&lt;UUID&gt;}.
 *
 * <p>This serializer writes the UUID contained in an {@code Optional<UUID>}
 * as a JSON string. If the optional is empty or null, the output is JSON
 * {@code null}. This ensures consistent and predictable persistence of
 * optional UUID fields.</p>
 *
 * <p>The output format matches the canonical representation produced by
 * {@link UUID#toString()}, guaranteeing compatibility with the corresponding
 * {@code OptionalUUIDDeserializer}.</p>
 */
public final class OptionalUUIDSerializer extends JsonSerializer<Optional<UUID>> {

    /**
     * Serializes an {@link Optional Optional&lt;UUID&gt;} into a JSON string or null.
     *
     * @param value the optional UUID to serialize
     * @param gen the JSON generator used to write the output
     * @param serializers the provider for additional serializers
     * @throws IOException if the output cannot be written
     */
    @Override
    public void serialize(Optional<UUID> value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {

        if (value == null || value.isEmpty()) {
            gen.writeNull();
        } else {
            gen.writeString(value.get().toString());
        }
    }

}
