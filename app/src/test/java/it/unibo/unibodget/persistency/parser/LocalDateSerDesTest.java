package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;

class LocalDateSerDesTest {

    private final ObjectMapper mapper = PersistenceJacksonConfig.mapper();

    @Test
    void testSerialize() throws Exception {
        final LocalDate date = LocalDate.of(2024, 5, 17);

        final String json = mapper.writeValueAsString(date);

        assertEquals("\"2024-05-17\"", json);
    }

    @Test
    void testDeserialize() throws Exception {
        final LocalDate date = mapper.readValue("\"2024-05-17\"", LocalDate.class);

        assertEquals(LocalDate.of(2024, 5, 17), date);
    }
}
