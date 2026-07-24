package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.api.DataSerializerException;
import it.unibo.unibodget.persistency.parser.impl.JsonComplexDataSerializer;

class JsonComplexDataSerializerTest {

    @Test
    void testSerializesValidObject() throws DataSerializerException {
        JsonComplexDataSerializer<TestDto> serializer =
                new JsonComplexDataSerializer<>();

        TestDto dto = new TestDto("Prova", 42);

        String json = serializer.serialize(dto);

        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"Prova\""));
        assertTrue(json.contains("\"value\""));
        assertTrue(json.contains("42"));
    }

    @Test
    @SuppressWarnings("unused")
    void testThrowsOnUnserializableObject() {
        JsonComplexDataSerializer<Object> serializer =
                new JsonComplexDataSerializer<>();

        Object unserializable = new Object() {
            // unused field in test - suppressed
            private final Object circular = this;
        };

        assertThrows(DataSerializerException.class,
                () -> serializer.serialize(unserializable));
    }

}
