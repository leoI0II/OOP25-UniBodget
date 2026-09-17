package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;
import it.unibo.unibodget.persistency.parser.impl.JsonParserDispatcher;

class JsonParserDispatcherTest {

    @Test
    void testSimpleParse() throws DataParserException {
        final String json = """
            { "name": "Prova", "value": 10 }
        """;

        final TestDto dto = JsonParserDispatcher.parse(json, TestDto.class);

        assertEquals("Prova", dto.getName());
    }

    @Test
    void testSimpleSerialize() throws DataSerializerException {
        final TestDto dto = new TestDto("Prova", 10);

        final String json = JsonParserDispatcher.serialize(dto);

        assertTrue(json.contains("\"Prova\""));
    }

    @Test
    void testComplexParser() throws DataParserException {
        final String json = """
            [
                {"name": "a", "value": 1},
                {"name": "b", "value": 2}
            ]
        """;

        final TestDto[] array = JsonParserDispatcher.parse(json, TestDto[].class);
        final List<TestDto> list = List.of(array);

        assertEquals(2, list.size());
        assertEquals("a", list.get(0).getName());
        assertEquals("b", list.get(1).getName());
    }

}
