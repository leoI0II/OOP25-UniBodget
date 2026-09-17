package it.unibo.unibodget.persistency.parser.impl;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom Jackson deserializer for {@link LocalDate}.
 *
 * <p>
 * This deserializer converts ISO-8601 date strings (e.g., "2024-05-17")
 * into {@code LocalDate} instances. It is used by the persistence layer to
 * ensure that date values stored as strings in JSON files are correctly
 * reconstructed when loading application data.
 * </p>
 *
 * <p>
 * The expected input format is the default ISO representation produced by
 * {@code LocalDate.toString()}, which guarantees compatibility with the
 * corresponding {@code LocalDateSerializer}.
 * </p>
 */
public final class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    /**
     * Deserializes a JSON string into a {@link LocalDate}.
     *
     * @param parser the JSON parser providing the string value
     * @param ctxt the deserialization context
     * @return the parsed {@code LocalDate} instance
     * @throws IOException if the input cannot be read or parsed
     */
    @Override
    public LocalDate deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException {
        return LocalDate.parse(parser.getValueAsString());
    }

}
