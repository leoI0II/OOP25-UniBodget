package it.unibo.unibodget.persistency.parser.impl;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom Jackson serializer for {@link LocalDate}.
 *
 * <p>This serializer converts a {@code LocalDate} into its ISO-8601 string
 * representation (e.g., "2024-05-17"). It ensures that date values are written
 * to JSON files in a consistent and interoperable format.</p>
 *
 * <p>The output format matches the default representation produced by
 * {@code LocalDate.toString()}, guaranteeing compatibility with the
 * corresponding {@code LocalDateDeserializer} used during JSON loading.</p>
 */
public final class LocalDateSerializer extends JsonSerializer<LocalDate> {

    /**
     * Serializes a {@link LocalDate} into an ISO-8601 string.
     *
     * @param value the date to serialize
     * @param gen the JSON generator used to write the output
     * @param serializers the provider for additional serializers
     * @throws IOException if the output cannot be written
     */
    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeString(value.toString());
    }

}
