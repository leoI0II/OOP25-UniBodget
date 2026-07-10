package it.unibo.unibodget.model.currency;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom Jackson deserializer for {@link CurrencyUnit}.
 *
 * <p>This deserializer allows Jackson to convert a currency code stored
 * as a JSON string (e.g., "EUR", "USD", "JPY") into the corresponding
 * {@link CurrencyUnit} instance.</p>
 *
 * <p>The deserialization process delegates the lookup to
 * {@link CurrencyUnit#getByCode(String)}, ensuring that all currency
 * instances are resolved consistently through the application's
 * central registry.</p>
 */
public class CurrencyUnitDeserializer extends JsonDeserializer<CurrencyUnit> {

    /**
     * Deserializes a JSON string into a {@link CurrencyUnit}.
     *
     * @param p     the JSON parser positioned at the currency code
     * @param ctxt  the deserialization context
     * @return the resolved {@link CurrencyUnit} instance
     * @throws IOException if the JSON parser encounters an error
     */
    @Override
    public CurrencyUnit deserialize(
            JsonParser p,
            DeserializationContext ctxt) throws IOException {

        return CurrencyUnit.getByCode(p.getValueAsString());
    }
}
