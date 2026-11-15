package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class CurrencyUnitSerializerTest {

    @Test
    void shouldSerializeCurrencyUnitAsCode() throws IOException {
        final StringWriter writer = new StringWriter();
        final JsonGenerator gen = new JsonFactory().createGenerator(writer);

        final CurrencyUnitSerializer s = new CurrencyUnitSerializer();
        s.serialize(FiatCurrency.EUR, gen, null);
        gen.close();

        assertEquals("\"EUR\"", writer.toString());
    }
}
