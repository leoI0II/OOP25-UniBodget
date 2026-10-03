package it.unibo.unibodget.model.currency;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Restores a registered currency from a code or a JSON object.
 */
public class CurrencyUnitDeserializer extends JsonDeserializer<CurrencyUnit> {

    /**
     * Deserializes a currency code or JSON object into a registered currency.
     *
     * <p>Accepts either a string code or an object containing a {@code code}
     * field. Searches enum-based currencies first, then dynamically loaded
     * currencies.</p>
     *
     * @param parser the JSON parser
     * @param context the deserialization context
     * @return the registered currency matching the code
     * @throws IOException if parsing fails or the currency code is missing,
     *         blank or unknown
     */
    @Override
    public CurrencyUnit deserialize(
            final JsonParser parser,
            final DeserializationContext context) throws IOException {

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
            throw new IOException(
                    "Currency without a valid code: " + node
            );
        }

        // Search in enum: fiat, crypto e stock
        CurrencyUnit unit = CurrencyUnit.getByCode(code);

        // Fall back to dynamically loaded currencies
        if (unit == null) {
            unit = Currency.get(code);
        }

        if (unit == null) {
            throw new IOException(
                    "Unknown currency code in saved data: " + code
            );
        }

        System.out.println("[PERSISTENCE] Restored currency: "
                + unit.getCode()
                + " as " + unit.getClass().getSimpleName());

        return unit;
    }

}
