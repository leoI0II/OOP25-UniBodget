package it.unibo.unibodget.model.currency;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Restores a registered currency from a code or a JSON object. */
public final class CurrencyUnitDeserializer extends JsonDeserializer<CurrencyUnit> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyUnitDeserializer.class);

    /**
     * Deserializes a currency code or JSON object into a registered currency.
     *
     * <p>Accepts either a string code or an object containing a {@code code} field. Searches
     * enum-based currencies first, then dynamically loaded currencies.
     *
     * @param parser the JSON parser
     * @param context the deserialization context
     * @return the registered currency matching the code
     * @throws IOException if parsing fails or the currency code is missing, blank or unknown
     */
    @Override
    public CurrencyUnit deserialize(final JsonParser parser, final DeserializationContext context)
            throws IOException {

        final JsonNode node = parser.getCodec().readTree(parser);

        final String code;
        if (node.isTextual()) {
            code = node.asText();
        } else if (node.isObject()) {
            code = node.path("code").asText(null);
        } else {
            code = null;
        }

        if (code == null || code.isBlank()) {
            throw new IOException("Currency without a valid code: " + node);
        }

        // Search in enum: fiat, crypto e stock
        CurrencyUnit unit = CurrencyUnit.getByCode(code);

        // Fall back to dynamically loaded currencies
        if (unit == null) {
            unit = Currency.get(code);
        }

        if (unit == null) {
            throw new IOException("Unknown currency code in saved data: " + code);
        }

        LOGGER.debug(
                "[PERSISTENCE] Restored currency: {} as {}",
                unit.getCode(),
                unit.getClass().getSimpleName());

        return unit;
    }
}
