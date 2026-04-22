package it.unibo.unibodget.persistency.reader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.reader.api.FileReader;
import it.unibo.unibodget.persistency.reader.impl.FileReaderFactory;
import it.unibo.unibodget.persistency.reader.impl.JsonReader;
import it.unibo.unibodget.persistency.util.TestCleanupUtils;

/**
 * Unit test class for {@link FileReaderFactory}.
 *
 * This test suite verifies that:
 * - unsupported extensions trigger an {@link IllegalArgumentException}
 * - missing extensions also trigger an {@link IllegalArgumentException}
 * - JSON files correctly produce a {@link JsonReader}
 * - the created reader can successfully read the content of a valid JSON file
 */
class FileReaderFactoryTest {

    private Path tempJsonFile;
    private Path tempTxtFile;

    /**
     * Creates a temporary JSON file before each test.
     * This ensures that the tests operate on a valid, isolated file.
     */
    @BeforeEach
    void setUp() throws IOException {
        createJsonFile();
        createTxtFile();
    }

    /**
     * This test is useful for debugging purposes, to verify that the temporary files are created correctly
     * and contain the expected content. It can be commented out or removed in production.
     * /
    @Test
    void debugPrintTempFiles() throws IOException {
        System.out.println("JSON file created at: " + tempJsonFile.toAbsolutePath());
        System.out.println("JSON file content: " + Files.readString(tempJsonFile));
        System.out.println("TXT file created at: " + tempTxtFile.toAbsolutePath());
        System.out.println("TXT file content: " + Files.readString(tempTxtFile));
    }
    */

    /**
     * Creates a temporary JSON file for testing.
     */
    private void createJsonFile() throws IOException {
        tempJsonFile = Files.createTempFile("test_json", ".json");
        Files.writeString(tempJsonFile, "{ \"key\": \"value\" }");
    }

    /**
     * Creates a temporary TXT file for testing.
     */
    private void createTxtFile() throws IOException {
        tempTxtFile = Files.createTempFile("test_txt", ".txt");
        Files.writeString(tempTxtFile, "This is a test file.");
    }

    /**
     * Verifies that FileReaderFactory returns a JsonReader
     * when the provided path has a .json extension.
     */
    @Test
    void testCreateJsonReader() {
        FileReader<?> reader = FileReaderFactory.create(tempJsonFile.toString());
        assertTrue(reader instanceof JsonReader);
    }

    /**
     * Verifies that FileReaderFactory throws an exception
     * when the file extension is unsupported.
     */
    @Test
    void testUnsupportedExtensionThrowsException() {
        String fakePath = "file.unsupported";

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> FileReaderFactory.create(fakePath)
        );

        assertTrue(ex.getMessage().contains("Unsupported file type"));
    }

    /**
     * Verifies that FileReaderFactory throws an exception
     * when the file path has no extension.
     */
    @Test
    void testMissingExtensionThrowsException() {
        String noExtPath = "file";

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> FileReaderFactory.create(noExtPath)
        );

        assertTrue(ex.getMessage().contains("extension"));
    }

    /**
     * Verifies that the JsonReader created by the factory
     * can successfully read the content of a valid JSON file.
     */
    @Test
    void testReaderReadsFileCorrectly() throws IOException {
        FileReader<?> reader = FileReaderFactory.create(tempJsonFile.toString());
        Object content = reader.readFile();

        assertNotNull(content);
        assertTrue(content.toString().contains("key"));
        assertTrue(content.toString().contains("value"));
    }

    /**
     * Cleans up all temporary files created during the test execution.
     *
     * This method is executed after each test and ensures that no temporary
     * resources remain on the filesystem. It uses {@link TestCleanupUtils}
     * to safely delete files or directories
     */
    @AfterEach
    void tearDown() {
        TestCleanupUtils.deleteRecursively(tempJsonFile);
        TestCleanupUtils.deleteRecursively(tempTxtFile);
    }

}
