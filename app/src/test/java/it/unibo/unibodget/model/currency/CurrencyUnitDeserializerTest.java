package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

class CurrencyUnitDeserializerTest {

    @Test
    void shouldDeserializeToPlaceholder() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonParser parser = mapper.getFactory().createParser("\"EUR\"");

        parser.nextToken();

        final CurrencyUnitDeserializer d = new CurrencyUnitDeserializer();
        final CurrencyUnit unit = d.deserialize(parser, null);

        assertTrue(unit instanceof CurrencyPlaceholder);
        assertEquals("EUR", unit.getCode());
    }

}
