package it.unibo.unibodget.model.currency;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom Jackson serializer for {@link CurrencyUnit}.
 *
 * <p>
 * This serializer ensures that {@link CurrencyUnit} instances are
 * written to JSON as simple currency codes (e.g., "EUR", "USD", "JPY"),
 * instead of being expanded into full objects.</p>
 *
 * <p>
 * The serialization process delegates the conversion to
 * {@link CurrencyUnit#getCode()}, guaranteeing a compact and stable
 * JSON representation.</p>
 */
public class CurrencyUnitSerializer extends JsonSerializer<CurrencyUnit> {

    /**
     * Serializes a {@link CurrencyUnit} into its currency code.
     *
     * @param value         the currency unit to serialize
     * @param gen           the JSON generator used to write output
     * @param serializers   the serializer provider
     * @throws IOException if writing to JSON fails
     */
    @Override
    public void serialize(
            final CurrencyUnit value,
            final JsonGenerator gen,
            final SerializerProvider serializers) 
        throws IOException {
        gen.writeString(value.getCode());
    }
}
