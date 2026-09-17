package it.unibo.unibodget.persistency.filemanager.opener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.filemanager.impl.opener.SafeFileOpener;

class SafeFileOpenerTest {

    private static final String SUFF_TXT = ".txt";

    private final SafeFileOpener opener = new SafeFileOpener();

    @Test
    void testThrowsOnNullPath() {
        assertThrows(IllegalArgumentException.class, () -> opener.open(null));
    }

    @Test
    void testCreatesMissingDirectoryAndFile() throws IOException {
        final Path tempDir = Files.createTempDirectory("safeOpenerTest");
        final Path file = tempDir.resolve("subdir").resolve("test.txt");

        assertFalse(Files.exists(file));
        assertFalse(Files.exists(file.getParent()));

        final Path returned = opener.open(file);

        assertEquals(file, returned);
        assertTrue(Files.exists(file.getParent()));
        assertTrue(Files.exists(file));
    }

    @Test
    void testReturnsExistingFile() throws IOException {
        final Path tempFile = Files.createTempFile("existingSafe", SUFF_TXT);

        assertTrue(Files.exists(tempFile));

        final Path returned = opener.open(tempFile);

        assertEquals(tempFile, returned);
    }

    @Test
    void testThrowsIfFileNotReadable() throws IOException {
        final Path tempFile = Files.createTempFile("notReadable", SUFF_TXT);

        // remove read permission
        tempFile.toFile().setReadable(false);

        assertThrows(IOException.class, () -> opener.open(tempFile));
    }

    @Test
    void testThrowsIfFileNotWritable() throws IOException {
        final Path tempFile = Files.createTempFile("notWritable", SUFF_TXT);

        // remove write permission
        tempFile.toFile().setWritable(false);

        assertThrows(IOException.class, () -> opener.open(tempFile));
    }
}
