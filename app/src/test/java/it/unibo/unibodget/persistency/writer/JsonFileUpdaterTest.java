package it.unibo.unibodget.persistency.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

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
        final Path temp = Files.createTempFile("update", ".json");
        Files.writeString(temp, "{ \"a\": 1 }");

        final JsonFileUpdater updater = new JsonFileUpdater();
        updater.update(temp, old -> old.replace("1", "2"));

        assertEquals("{ \"a\": 2 }", Files.readString(temp));
    }

    /**
     * Ensures that updating a non-existing file throws an exception.
     */
    @Test
    void testUpdateFailsIfMissing() {
        final Path temp = Path.of("missing-file.json");

        final JsonFileUpdater updater = new JsonFileUpdater();

        assertThrows(IOException.class, () ->
            updater.update(temp, old -> "X")
        );
    }
}
