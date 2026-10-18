package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OptionalUUIDSerDesTest {

    private static final String BACKSLASH = "\"";
    private final ObjectMapper mapper = PersistenceJacksonConfig.mapper();

    @Test
    void testSerializeEmpty() throws JsonProcessingException {
        final String json = mapper.writeValueAsString(Optional.empty());
        assertEquals("null", json);
    }

    @Test
    void testSerializeUUID() throws JsonProcessingException {
        final UUID id = UUID.randomUUID();
        final String json = mapper.writeValueAsString(Optional.of(id));
        assertEquals(BACKSLASH + id.toString() + BACKSLASH, json);
    }

    @Test
    void testDeserializeEmpty() throws JsonProcessingException {
        final Optional<UUID> opt =
                mapper.readValue(
                        "null",
                        new TypeReference<>() {
                            /* empty */
                        });

        assertTrue(opt.isEmpty());
    }

    @Test
    void testDeserializeUUID() throws JsonProcessingException {
        final UUID id = UUID.randomUUID();
        final Optional<UUID> opt =
                mapper.readValue(
                        BACKSLASH + id + BACKSLASH,
                        new TypeReference<>() {
                            /* empty */
                        });

        assertEquals(id, opt.get());
    }
}
