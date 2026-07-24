package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.parser.impl.JsonComplexDataParser;

class JsonComplexDataParserTest {

    @Test
    void testParsesValidJson() throws DataParserException {
        JsonComplexDataParser<TestDto> parser =
                new JsonComplexDataParser<>(TestDto.class);

        String json = """
            {
                "name": "Prova",
                "value": 42
            }
            """;

        TestDto dto = parser.parse(json);

        assertEquals("Prova", dto.name);
        assertEquals(42, dto.value);
    }

    @Test
    void testThrowsOnMalformedJson() {
        JsonComplexDataParser<TestDto> parser =
                new JsonComplexDataParser<>(TestDto.class);

        String malformed = "{ invalid json }";

        assertThrows(DataParserException.class, () -> parser.parse(malformed));
    }

    @Test
    void testThrowsOnTypeMismatch() {
        JsonComplexDataParser<TestDto> parser =
                new JsonComplexDataParser<>(TestDto.class);

        String wrongTypeJson = """
            {
                "name": "Prova",
                "value": "string"
            }
            """;

        assertThrows(DataParserException.class, () -> parser.parse(wrongTypeJson));
    }
}
