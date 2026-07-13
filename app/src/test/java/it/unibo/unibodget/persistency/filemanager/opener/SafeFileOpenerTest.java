package it.unibo.unibodget.persistency.filemanager.opener;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.filemanager.impl.opener.SafeFileOpener;

class SafeFileOpenerTest {

    private final SafeFileOpener opener = new SafeFileOpener();

    @Test
    void testThrowsOnNullPath() {
        assertThrows(IllegalArgumentException.class, () -> opener.open(null));
    }

    @Test
    void testCreatesMissingDirectoryAndFile() throws IOException {
        Path tempDir = Files.createTempDirectory("safeOpenerTest");
        Path file = tempDir.resolve("subdir").resolve("test.txt");

        assertFalse(Files.exists(file));
        assertFalse(Files.exists(file.getParent()));

        Path returned = opener.open(file);

        assertEquals(file, returned);
        assertTrue(Files.exists(file.getParent()));
        assertTrue(Files.exists(file));
    }

    @Test
    void testReturnsExistingFile() throws IOException {
        Path tempFile = Files.createTempFile("existingSafe", ".txt");

        assertTrue(Files.exists(tempFile));

        Path returned = opener.open(tempFile);

        assertEquals(tempFile, returned);
    }

    @Test
    void testThrowsIfFileNotReadable() throws IOException {
        Path tempFile = Files.createTempFile("notReadable", ".txt");

        // remove read permission
        tempFile.toFile().setReadable(false);

        assertThrows(IOException.class, () -> opener.open(tempFile));
    }

    @Test
    void testThrowsIfFileNotWritable() throws IOException {
        Path tempFile = Files.createTempFile("notWritable", ".txt");

        // remove write permission
        tempFile.toFile().setWritable(false);

        assertThrows(IOException.class, () -> opener.open(tempFile));
    }
}
