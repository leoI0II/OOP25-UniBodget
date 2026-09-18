package it.unibo.unibodget.persistency.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.writer.impl.JsonFileOverwriter;

/**
 * Unit tests for {@link JsonFileOverwriter}.
 */
class JsonFileOverwriterTest {

    /**
     * Ensures that overwriting a file replaces its entire content.
     */
    @Test
    void testOverwrite() throws IOException {
        final Path temp = Files.createTempFile("overwrite", ".json");
        Files.writeString(temp, "OLD");

        final JsonFileOverwriter overwriter = new JsonFileOverwriter();
        overwriter.overwrite(temp, "NEW");

        assertEquals("NEW", Files.readString(temp));
    }
}
