package it.unibo.unibodget.persistency.reader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.unibodget.persistency.reader.impl.BasicReader;
import it.unibo.unibodget.persistency.reader.impl.JsonReader;
import it.unibo.unibodget.persistency.util.TestCleanupUtils;

/**
 * Unit test class for {@link JsonReader}.
 *
 * This test suite verifies that:
 * - JsonReader correctly reads the content of a valid JSON file
 * - JsonReader throws exceptions when the file does not exist
 * - JsonReader throws exceptions when the path is invalid or blank
 * - JsonReader inherits and respects the validation rules of {@link BasicReader}
 */
class JsonReaderTest {

    private Path tempJsonFile;

    /**
     * Creates a temporary JSON file before each test.
     * This ensures that each test operates on a valid, isolated file.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempJsonFile = Files.createTempFile("test_json_reader", ".json");
        Files.writeString(tempJsonFile, "{ \"name\": \"Prova\", \"value\": 42 }");
    }

    /**
     * Verifies that JsonReader successfully reads the content
     * of a valid JSON file and returns it as a String.
     */
    @Test
    void testReadFileReturnsCorrectContent() throws IOException {
        JsonReader reader = new JsonReader(tempJsonFile.toString());
        String content = reader.readFile();

        assertNotNull(content);
        assertTrue(content.contains("Prova"));
        assertTrue(content.contains("42"));
    }

    /**
     * Verifies that JsonReader throws an IllegalArgumentException
     * when the provided file path does not exist.
     */
    @Test
    void testConstructorThrowsIfFileDoesNotExist() {
        String invalidPath = "non_existing_file.json";

        assertThrows(IllegalArgumentException.class, () -> new JsonReader(invalidPath));
    }

    /**
     * Verifies that JsonReader throws an IllegalArgumentException
     * when the provided path is blank.
     */
    @Test
    void testConstructorThrowsIfPathIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new JsonReader(" "));
    }

    /**
     * Verifies that JsonReader throws an IllegalArgumentException
     * when the provided path refers to a directory instead of a file.
     */
    @Test
    void testConstructorThrowsIfPathIsDirectory() throws IOException {
        Path directory = Files.createTempDirectory("json_reader_test_dir");

        assertThrows(IllegalArgumentException.class, () -> new JsonReader(directory.toString()));
    }

    /**
     * Verifies that JsonReader throws an IllegalArgumentException
     * when the file exists but is not readable
     * 
     * This happens because BasicReader performs validation in the constructor:
     * if the file cannot be read, the JsonReader instance cannot be created
     */
    @Test
    void testReadFileThrowsIfFileNotReadable() throws IOException {
        tempJsonFile.toFile().setReadable(false);

        assertThrows(IllegalArgumentException.class,
            () -> new JsonReader(tempJsonFile.toString())
        );

        // Ripristina i permessi per evitare problemi su Windows
        tempJsonFile.toFile().setReadable(true);
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
    }
}
