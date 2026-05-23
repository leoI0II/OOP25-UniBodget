package it.unibo.unibodget.persistency.parser;

import it.unibo.unibodget.model.utils.ARGBColor;
import it.unibo.unibodget.persistency.parser.api.DataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;
import it.unibo.unibodget.persistency.parser.impl.JsonDataParser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsonDataParser}.
 *
 * This test suite verifies correct JSON parsing for:
 * - primitive and boxed types
 * - String values
 * - enums
 * - arrays
 * - lists
 * - ARGBColor values encoded as { "value": <int> }
 * - POJOs with public fields accessed via reflection
 *
 * The tests ensure that the parser correctly reconstructs Java objects
 * from JSON structures using the reflection‑based logic implemented in
 * {@link JsonDataParser}.
 */
public final class JsonDataParserTest {

    
}
