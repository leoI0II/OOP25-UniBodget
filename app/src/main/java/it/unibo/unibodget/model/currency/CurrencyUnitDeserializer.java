package it.unibo.unibodget.model.currency;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Simple Jackson deserializer for {@link CurrencyUnit}.
 *
 * <p>
 * This deserializer only reads the currency code from JSON.
 * The actual lookup (mapping code → Currency instance) is performed
 * later by {@link Currency#init()} when all currencies are loaded.
 * </p>
 *
 * <p>
 * This avoids recursive initialization loops during JSON parsing.
 * </p>
 */
public class CurrencyUnitDeserializer extends JsonDeserializer<CurrencyUnit> {

    @Override
    public CurrencyUnit deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException {

        // Read the code only — do NOT call Currency.get() here.
        final String code = p.getValueAsString();

        // Create a lightweight placeholder CurrencyUnit.
        // Currency.init() will replace these with real Currency objects.
        System.out.println("DEBUG DESERIALIZER → code = " + p.getValueAsString());

        return new CurrencyPlaceholder(code);
    }
}
