package it.unibo.unibodget.persistency.serializer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.impl.JsonDataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.utils.ARGBColor;

import java.util.List;

/**
 * Unit tests for {@link JsonDataSerializer}.
 *
 * These tests verify that the serializer correctly converts Java objects into
 * JSON strings, including:
 * - primitive fields
 * - enums
 * - nested objects
 * - lists
 * - real UniBodget model classes
 */
public class JsonDataSerializerTest {

    /** Simple record used to test primitive serialization. */
    record User(String name, int age, boolean active) {}

    /**
     * Ensures that primitive fields are correctly serialized into JSON.
     */
    @Test
    void testSerializePrimitives() throws DataSerializerException {
        User u = new User("Arianna", 23, true);
        JsonDataSerializer<User> serializer = new JsonDataSerializer<>(User.class);

        String json = serializer.serialize(u);

        assertTrue(json.contains("\"name\": \"Arianna\""));
        assertTrue(json.contains("\"age\": 23"));
        assertTrue(json.contains("\"active\": true"));
    }

    /**
     * Ensures that enum values are serialized as their name().
     */
    @Test
    void testSerializeEnum() throws DataSerializerException {
        record Wrapper(CategoryType type) {}

        Wrapper w = new Wrapper(CategoryType.EXPENSE);
        JsonDataSerializer<Wrapper> serializer = new JsonDataSerializer<>(Wrapper.class);

        String json = serializer.serialize(w);

        assertTrue(json.contains("\"type\": \"EXPENSE\""));
    }

    /**
     * Ensures that nested objects (e.g., ARGBColor) are serialized correctly.
     */
    @Test
    void testSerializeNestedObject() throws DataSerializerException {
        record Wrapper(ARGBColor color) {}

        Wrapper w = new Wrapper(new ARGBColor(0xFFFFFFFF));
        JsonDataSerializer<Wrapper> serializer = new JsonDataSerializer<>(Wrapper.class);

        String json = serializer.serialize(w);

        assertTrue(json.contains("\"value\": 4294967295"));
    }

    /**
     * Ensures that lists are serialized as JSON arrays.
     */
    @Test
    void testSerializeList() throws DataSerializerException {
        record Wrapper(List<Integer> values) {}

        Wrapper w = new Wrapper(List.of(1, 2, 3));
        JsonDataSerializer<Wrapper> serializer = new JsonDataSerializer<>(Wrapper.class);

        String json = serializer.serialize(w);

        assertTrue(json.contains("["));
        assertTrue(json.contains("1"));
        assertTrue(json.contains("2"));
        assertTrue(json.contains("3"));
    }

    /**
     * Ensures that a real UniBodget model class ({@link Category})
     * is serialized correctly.
     */
    @Test
    void testSerializeCategory() throws DataSerializerException {
        Category c = new Category("Food", new ARGBColor(0xFFFFFFFF), CategoryType.EXPENSE);
        JsonDataSerializer<Category> serializer = new JsonDataSerializer<>(Category.class);

        String json = serializer.serialize(c);

        assertTrue(json.contains("\"name\": \"Food\""));
        assertTrue(json.contains("\"type\": \"EXPENSE\""));
        assertTrue(json.contains("\"value\": 4294967295"));
    }
}
