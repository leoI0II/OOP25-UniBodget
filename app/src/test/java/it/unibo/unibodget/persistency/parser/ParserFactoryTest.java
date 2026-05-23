package it.unibo.unibodget.persistency.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.parser.impl.ParserFactory;
import it.unibo.unibodget.persistency.parser.impl.JsonDataParser;
import it.unibo.unibodget.persistency.parser.api.DataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;

import it.unibo.unibodget.model.categories.Category;

/**
 * Unit tests for {@link ParserFactory}.
 *
 * These tests verify that:
 * - JSON files produce a {@link JsonDataParser}
 * - unsupported file extensions trigger a {@link DataParserException}
 */
public class ParserFactoryTest {

    /**
     * Ensures that requesting a parser for a .json file
     * returns a {@link JsonDataParser} instance.
     */
    @Test
    void testJsonParserCreation() throws DataParserException {
        DataParser<Category> parser =
            ParserFactory.of(Category.class, "categories.json");

        assertNotNull(parser);
        assertTrue(parser instanceof JsonDataParser);
    }

    /**
     * Ensures that unsupported file extensions cause a {@link DataParserException}.
     */
    @Test
    void testUnsupportedExtension() {
        assertThrows(DataParserException.class, () ->
            ParserFactory.of(Category.class, "data.txt")
        );
    }
}
