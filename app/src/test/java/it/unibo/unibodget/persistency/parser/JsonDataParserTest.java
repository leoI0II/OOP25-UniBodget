package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.*;

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
        JsonDataParser<TestDto> parser = new JsonDataParser<>(TestDto.class);

        String json = """
            { "name": "Arianna", "value": 10 }
        """;

        TestDto dto = parser.parse(json);

        assertEquals("Arianna", dto.name);
        assertEquals(10, dto.value);
    }

    @Test
    void testParsesList() throws DataParserException {
        JsonDataParser<TestDto> parser = new JsonDataParser<>(TestDto.class);

        String json = """
            [
                { "name": "A", "value": 1 },
                { "name": "B", "value": 2 }
            ]
        """;

        List<TestDto> list = parser.parseList(json);

        assertEquals(2, list.size());
        assertEquals("A", list.get(0).name);
        assertEquals(2, list.get(1).value);
    }

    @Test
    void testParseListFromFile() throws DataParserException, IOException {
        Path temp = Files.createTempFile("json", ".txt");
        Files.writeString(temp, """
            {
                "items": [
                    { "name": "A", "value": 1 },
                    { "name": "B", "value": 2 }
                ]
            }
        """);

        JsonDataParser<TestDto> parser = new JsonDataParser<>(TestDto.class);

        List<TestDto> list = parser.parseListFromFile(temp, "items");

        assertEquals(2, list.size());
        assertEquals("B", list.get(1).name);
    }

    @Test
    void testMalformedJsonThrows() {
        JsonDataParser<TestDto> parser = new JsonDataParser<>(TestDto.class);

        assertThrows(DataParserException.class,
            () -> parser.parse("{ \"value\": notANumber }"));
    }

}
