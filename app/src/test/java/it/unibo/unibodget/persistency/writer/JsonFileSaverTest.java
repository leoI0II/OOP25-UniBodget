package it.unibo.unibodget.persistency.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.writer.impl.JsonFileSaver;

/**
 * Unit tests for {@link JsonFileSaver}.
 */
class JsonFileSaverTest {

    /**
     * Verifies that saving creates a new file with the expected content.
     */
    @Test
    void testSaveCreatesFile() throws IOException {
        final Path temp = Files.createTempDirectory("save").resolve("file.json");

        final JsonFileSaver saver = new JsonFileSaver();
        saver.save(temp, "{ \"a\": 1 }");

        assertTrue(Files.exists(temp));
        assertEquals("{ \"a\": 1 }", Files.readString(temp));
    }

    /**
     * Verifies that saving fails if the file already exists.
     */
    @Test
    void testSaveFailsIfExists() throws IOException {
        final Path temp = Files.createTempFile("exists", ".json");

        final JsonFileSaver saver = new JsonFileSaver();

        assertThrows(IOException.class, () ->
            saver.save(temp, "X")
        );
    }
}
