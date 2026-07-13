package it.unibo.unibodget.persistency.filemanager.creator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.filemanager.impl.creator.JsonFileCreator;

class JsonFileCreatorTest {

    private final JsonFileCreator creator = new JsonFileCreator();

    @Test
    void testOpenThrowsOnNullPath() {
        assertThrows(IllegalArgumentException.class, () -> creator.open(null));
    }

    @Test
    void testCreatesJsonFileWithEmptyObject() throws IOException {
        Path tempDir = Files.createTempDirectory("jsonCreatorTest");
        Path jsonFile = tempDir.resolve("new.json");

        assertFalse(Files.exists(jsonFile));

        creator.open(jsonFile);

        assertTrue(Files.exists(jsonFile));
        assertEquals("{}", Files.readString(jsonFile));
    }

    @Test
    void testDoesNotOverwriteExistingJsonFile() throws IOException {
        Path tempFile = Files.createTempFile("existingJson", ".json");
        Files.writeString(tempFile, "{\"a\":1}");

        creator.open(tempFile);

        assertEquals("{\"a\":1}", Files.readString(tempFile));
    }
}
