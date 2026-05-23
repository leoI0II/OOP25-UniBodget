package it.unibo.unibodget.persistency.writer;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.*;

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
        Path temp = Files.createTempFile("append", ".json");
        Files.writeString(temp, "A");

        JsonFileAppender appender = new JsonFileAppender();
        appender.append(temp, "B");

        assertEquals("AB", Files.readString(temp));
    }

    /**
     * Ensures that appending to a non-existing file creates it automatically.
     */
    @Test
    void testAppendCreatesFileIfMissing() throws IOException {
        Path dir = Files.createTempDirectory("append-missing");
        Path file = dir.resolve("new.json");

        JsonFileAppender appender = new JsonFileAppender();
        appender.append(file, "DATA");

        assertTrue(Files.exists(file));
        assertEquals("DATA", Files.readString(file));
    }
}
