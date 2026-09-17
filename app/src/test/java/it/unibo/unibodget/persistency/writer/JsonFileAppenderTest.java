package it.unibo.unibodget.persistency.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.writer.impl.JsonFileAppender;

/**
 * Unit tests for {@link JsonFileAppender}.
 */
class JsonFileAppenderTest {

    /**
     * Ensures that appending content adds data at the end of the file.
     */
    @Test
    void testAppendAppendsContent() throws IOException {
        final Path temp = Files.createTempFile("append", ".json");
        Files.writeString(temp, "A");

        final JsonFileAppender appender = new JsonFileAppender();
        appender.append(temp, "B");

        assertEquals("AB", Files.readString(temp));
    }

    /**
     * Ensures that appending to a non-existing file creates it automatically.
     */
    @Test
    void testAppendCreatesFileIfMissing() throws IOException {
        final Path dir = Files.createTempDirectory("append-missing");
        final Path file = dir.resolve("new.json");

        final JsonFileAppender appender = new JsonFileAppender();
        appender.append(file, "DATA");

        assertTrue(Files.exists(file));
        assertEquals("DATA", Files.readString(file));
    }
}
