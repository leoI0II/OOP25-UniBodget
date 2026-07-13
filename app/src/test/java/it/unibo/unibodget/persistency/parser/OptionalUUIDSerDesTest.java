package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;

class OptionalUUIDSerDesTest {

    private final ObjectMapper mapper = PersistenceJacksonConfig.mapper();

    @Test
    void testSerializeEmpty() throws Exception {
        String json = mapper.writeValueAsString(Optional.empty());
        assertEquals("null", json);
    }

    @Test
    void testSerializeUUID() throws Exception {
        UUID id = UUID.randomUUID();
        String json = mapper.writeValueAsString(Optional.of(id));
        assertEquals("\"" + id.toString() + "\"", json);
    }

    @Test
    void testDeserializeEmpty() throws Exception {
        Optional<UUID> opt = mapper.readValue(
        "null",
        new TypeReference<Optional<UUID>>() {}
    );
        assertTrue(opt.isEmpty());
    }

    @Test
    void testDeserializeUUID() throws Exception {
        UUID id = UUID.randomUUID();
        Optional<UUID> opt = mapper.readValue(
            "\"" + id + "\"",
            new TypeReference<Optional<UUID>>() {}
        );
        assertEquals(id, opt.get());
    }

}
