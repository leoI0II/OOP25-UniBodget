package it.unibo.unibodget.persistency.filemanager.creator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.filemanager.api.FileCreator;
import it.unibo.unibodget.persistency.filemanager.impl.creator.FileCreatorFactory;
import it.unibo.unibodget.persistency.filemanager.impl.creator.JsonFileCreator;

class FileCreatorFactoryTest {

    @Test
    void testCreatesJsonFileCreator() {
        FileCreator creator = FileCreatorFactory.create("file.json");
        assertTrue(creator instanceof JsonFileCreator);
    }

    @Test
    void testThrowsOnUnsupportedExtension() {
        assertThrows(IllegalArgumentException.class, () -> FileCreatorFactory.create("file.txt"));
    }

    @Test
    void testThrowsOnMissingExtension() {
        assertThrows(IllegalArgumentException.class, () -> FileCreatorFactory.create("file"));
    }
}
