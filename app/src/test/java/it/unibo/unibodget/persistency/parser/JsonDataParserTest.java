package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.parser.impl.JsonDataParser;

class JsonDataParserTest {

    @Test
    void testParsesSimpleObject() throws DataParserException {
        final JsonDataParser<TestDto> parser = new JsonDataParser<>(TestDto.class);

        final String json = """
            { "name": "Prova", "value": 10 }
        """;

        final TestDto dto = parser.parse(json);

        assertEquals("Prova", dto.getName());
        assertEquals(10, dto.getValue());
    }

    @Test
    void testParsesList() throws DataParserException {
        final JsonDataParser<TestDto> parser = new JsonDataParser<>(TestDto.class);

        final String json = """
            [
                { "name": "A", "value": 1 },
                { "name": "B", "value": 2 }
            ]
        """;

        final List<TestDto> list = parser.parseList(json);

        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getName());
        assertEquals(2, list.get(1).getValue());
    }

    @Test
    void testParseListFromFile() throws DataParserException, IOException {
        final Path temp = Files.createTempFile("json", ".txt");
        Files.writeString(temp, """
            {
                "items": [
                    { "name": "A", "value": 1 },
                    { "name": "B", "value": 2 }
                ]
            }
        """);

        final JsonDataParser<TestDto> parser = new JsonDataParser<>(TestDto.class);

        final List<TestDto> list = parser.parseListFromFile(temp, "items");

        assertEquals(2, list.size());
        assertEquals("B", list.get(1).getName());
    }

    @Test
    void testMalformedJsonThrows() {
        final JsonDataParser<TestDto> parser = new JsonDataParser<>(TestDto.class);

        assertThrows(DataParserException.class,
            () -> parser.parse("{ \"value\": notANumber }"));
    }

}
