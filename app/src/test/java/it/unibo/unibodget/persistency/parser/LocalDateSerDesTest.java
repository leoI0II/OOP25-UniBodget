package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LocalDateSerDesTest {

    private static final int YEAR_2024 = 2024;
    private static final int MONTH_5 = 5;
    private static final int DAY_OF_M_17 = 17;

    private final ObjectMapper mapper = PersistenceJacksonConfig.mapper();

    @Test
    void testSerialize() throws JsonProcessingException {
        final LocalDate date = LocalDate.of(YEAR_2024, MONTH_5, DAY_OF_M_17);

        final String json = mapper.writeValueAsString(date);

        assertEquals("\"2024-05-17\"", json);
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        final LocalDate date = mapper.readValue("\"2024-05-17\"", LocalDate.class);

        assertEquals(LocalDate.of(YEAR_2024, MONTH_5, DAY_OF_M_17), date);
    }
}
