package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.parser.impl.JsonComplexDataParser;

class JsonComplexDataParserTest {

    private static final int NUMB_42 = 42;
    private static final String PROVA = "Prova";

    @Test
    void testParsesValidJson() throws DataParserException {
        final JsonComplexDataParser<TestDto> parser =
                new JsonComplexDataParser<>(TestDto.class);

        final String json = """
            {
                "name": "Prova",
                "value": 42
            }
            """;

        final TestDto dto = parser.parse(json);

        assertEquals(PROVA, dto.getName());
        assertEquals(NUMB_42, dto.getValue());
    }

    @Test
    void testThrowsOnMalformedJson() {
        final JsonComplexDataParser<TestDto> parser =
                new JsonComplexDataParser<>(TestDto.class);

        final String malformed = "{ invalid json }";

        assertThrows(DataParserException.class, () -> parser.parse(malformed));
    }

    @Test
    void testThrowsOnTypeMismatch() {
        final JsonComplexDataParser<TestDto> parser =
                new JsonComplexDataParser<>(TestDto.class);

        final String wrongTypeJson = """
            {
                "name": "Prova",
                "value": "string"
            }
            """;

        assertThrows(DataParserException.class, () -> parser.parse(wrongTypeJson));
    }
}
