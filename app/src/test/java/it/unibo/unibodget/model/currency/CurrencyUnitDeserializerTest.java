package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class CurrencyUnitDeserializerTest {

    @Test
    void shouldDeserializeRegisteredCurrency() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        try (JsonParser parser = mapper.getFactory().createParser("\"EUR\"")) {
            parser.nextToken();

            final CurrencyUnitDeserializer deserializer = new CurrencyUnitDeserializer();
            final CurrencyUnit unit = deserializer.deserialize(parser, null);

            assertEquals(FiatCurrency.EUR, unit);
            assertEquals("EUR", unit.getCode());
        }
    }
}
