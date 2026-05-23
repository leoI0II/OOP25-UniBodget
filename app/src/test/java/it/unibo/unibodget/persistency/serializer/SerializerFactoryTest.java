package it.unibo.unibodget.persistency.serializer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.impl.JsonDataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;
import it.unibo.unibodget.persistency.parser.impl.SerializerFactory;

import it.unibo.unibodget.model.categories.Category;

/**
 * Unit tests for {@link SerializerFactory}.
 *
 * These tests verify that:
 * - JSON serializers are created correctly for .json files
 * - unsupported file extensions trigger a {@link DataSerializerException}
 */
public class SerializerFactoryTest {

    /**
     * Ensures that requesting a serializer for a .json file
     * returns a {@link JsonDataSerializer} instance.
     */
    @Test
    void testJsonSerializerCreation() throws DataSerializerException {
        DataSerializer<Category> serializer =
            SerializerFactory.create("categories.json", Category.class);

        assertNotNull(serializer);
        assertTrue(serializer instanceof JsonDataSerializer);
    }

    /**
     * Ensures that unsupported file extensions cause a {@link DataSerializerException}.
     */
    @Test
    void testUnsupportedExtension() {
        assertThrows(DataSerializerException.class, () ->
            SerializerFactory.create("data.txt", Category.class)
        );
    }
}
