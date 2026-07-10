package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

class CurrencyUnitDeserializerTest {

    @Test
    void shouldDeserializeToPlaceholder() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("\"EUR\"");

        // NECESSARIO: sposta il parser sul valore
        parser.nextToken();

        CurrencyUnitDeserializer d = new CurrencyUnitDeserializer();
        CurrencyUnit unit = d.deserialize(parser, null);

        assertTrue(unit instanceof CurrencyPlaceholder);
        assertEquals("EUR", unit.getCode());
    }

}
