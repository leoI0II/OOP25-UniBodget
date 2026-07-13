package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;
import it.unibo.unibodget.persistency.parser.impl.JsonParserDispatcher;

class JsonParserDispatcherTest {

    @Test
    void testSimpleParse() throws DataParserException {
        String json = """
            { "name": "Arianna", "value": 10 }
        """;

        TestDto dto = JsonParserDispatcher.parse(json, TestDto.class);

        assertEquals("Arianna", dto.name);
    }

    @Test
    void testSimpleSerialize() throws DataSerializerException {
        TestDto dto = new TestDto("Arianna", 10);

        String json = JsonParserDispatcher.serialize(dto);

        assertTrue(json.contains("\"Arianna\""));
    }

    @Test
    void testComplexParser() throws DataParserException {
        String json = """
            [
                {"name": "a", "value": 1},
                {"name": "b", "value": 2}
            ]
        """;

        TestDto[] array = JsonParserDispatcher.parse(json, TestDto[].class);
        List<TestDto> list = List.of(array);

        assertEquals(2, list.size());
        assertEquals("a", list.get(0).name);
        assertEquals("b", list.get(1).name);
    }

}
