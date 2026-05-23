package it.unibo.unibodget.persistency.writer;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.*;

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
        Path temp = Files.createTempDirectory("save").resolve("file.json");

        JsonFileSaver saver = new JsonFileSaver();
        saver.save(temp, "{ \"a\": 1 }");

        assertTrue(Files.exists(temp));
        assertEquals("{ \"a\": 1 }", Files.readString(temp));
    }

    /**
     * Verifies that saving fails if the file already exists.
     */
    @Test
    void testSaveFailsIfExists() throws IOException {
        Path temp = Files.createTempFile("exists", ".json");

        JsonFileSaver saver = new JsonFileSaver();

        assertThrows(IOException.class, () ->
            saver.save(temp, "X")
        );
    }
}
