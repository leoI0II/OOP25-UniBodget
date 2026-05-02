package it.unibo.unibodget.persistency.writer;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.*;

import it.unibo.unibodget.persistency.writer.impl.JsonFileUpdater;

/**
 * Unit tests for {@link JsonFileUpdater}.
 */
class JsonFileUpdaterTest {

    /**
     * Ensures that updating a file correctly transforms its content.
     */
    @Test
    void testUpdate() throws IOException {
        Path temp = Files.createTempFile("update", ".json");
        Files.writeString(temp, "{ \"a\": 1 }");

        JsonFileUpdater updater = new JsonFileUpdater();
        updater.update(temp, old -> old.replace("1", "2"));

        assertEquals("{ \"a\": 2 }", Files.readString(temp));
    }

    /**
     * Ensures that updating a non-existing file throws an exception.
     */
    @Test
    void testUpdateFailsIfMissing() {
        Path temp = Path.of("missing-file.json");

        JsonFileUpdater updater = new JsonFileUpdater();

        assertThrows(IOException.class, () ->
            updater.update(temp, old -> "X")
        );
    }
}
