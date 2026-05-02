package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.impl.JsonDataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.utils.ARGBColor;

import java.util.List;

/**
 * Unit tests for {@link JsonDataParser}.
 *
 * These tests verify that the parser correctly converts JSON strings into
 * Java objects, including:
 * - primitive values
 * - enums
 * - nested objects
 * - lists
 * - real UniBodget model classes (e.g., {@link Category})
 */
public class JsonDataParserTest {

    /** Simple record used to test primitive parsing. */
    record User(String name, int age, boolean active) {}

    /**
     * Ensures that primitive JSON fields (String, int, boolean)
     * are correctly parsed into a Java record.
     */
    @Test
    void testParsePrimitives() throws DataParserException {
        String json = """
        {
            "name": "Arianna",
            "age": 23,
            "active": true
        }
        """;

        JsonDataParser<User> parser = new JsonDataParser<>(User.class);
        User u = parser.parse(json);

        assertEquals("Arianna", u.name());
        assertEquals(23, u.age());
        assertTrue(u.active());
    }

    /**
     * Ensures that enum values are correctly resolved from their JSON string representation.
     */
    @Test
    void testParseEnum() throws DataParserException {
        record Wrapper(CategoryType type) {}

        String json = """
        {
            "type": "EXPENSE"
        }
        """;

        JsonDataParser<Wrapper> parser = new JsonDataParser<>(Wrapper.class);
        Wrapper w = parser.parse(json);

        assertEquals(CategoryType.EXPENSE, w.type());
    }

    /**
     * Ensures that nested objects (e.g., ARGBColor) are correctly parsed.
     */
    @Test
    void testParseNestedObject() throws DataParserException {
        record Wrapper(ARGBColor color) {}

        String json = """
        {
            "color": { "value": 4294967295 }
        }
        """;

        JsonDataParser<Wrapper> parser = new JsonDataParser<>(Wrapper.class);
        Wrapper w = parser.parse(json);

        assertEquals(new ARGBColor(0xFFFFFFFF), w.color());
    }

    /**
     * Ensures that JSON arrays are correctly parsed into Java Lists.
     */
    @Test
    void testParseList() throws DataParserException {
        record Wrapper(List<Integer> values) {}

        String json = """
        {
            "values": [1, 2, 3]
        }
        """;

        JsonDataParser<Wrapper> parser = new JsonDataParser<>(Wrapper.class);
        Wrapper w = parser.parse(json);

        assertEquals(List.of(1, 2, 3), w.values());
    }

    /**
     * Ensures that a real UniBodget model class ({@link Category})
     * is correctly reconstructed from JSON.
     */
    @Test
    void testParseCategory() throws DataParserException {
        String json = """
        {
            "name": "Food",
            "colorHex": { "value": 4294967295 },
            "type": "EXPENSE"
        }
        """;

        JsonDataParser<Category> parser = new JsonDataParser<>(Category.class);
        Category c = parser.parse(json);

        assertEquals("Food", c.getName());
        assertEquals(new ARGBColor(0xFFFFFFFF), c.getColorHex());
        assertEquals(CategoryType.EXPENSE, c.getType());
    }
}
