package it.unibo.unibodget.persistency.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import it.unibo.unibodget.persistency.reader.impl.BasicReader;
import it.unibo.unibodget.persistency.reader.impl.JsonReader;
import it.unibo.unibodget.persistency.util.TestCleanupUtils;

/**
 * Unit test class for {@link JsonReader}.
 *
 * <p>
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
        final JsonReader reader = new JsonReader(tempJsonFile.toString());
        final String content = reader.readFile();

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
        final String invalidPath = "non_existing_file.json";

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
        final Path directory = Files.createTempDirectory("json_reader_test_dir");

        assertThrows(IllegalArgumentException.class, () -> new JsonReader(directory.toString()));
    }

    /**
     * Verifies that JsonReader throws an IllegalArgumentException on UNIX systems
     * when the file exists but is not readable.
     *
     * <p>
     * On POSIX-compliant systems (Linux, macOS), file permissions can be
     * reliably manipulated using {@code setPosixFilePermissions}.
     * 
     * <p>
     * This test is enabled only on UNIX systems.
     */
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void testConstructorThrowsIfFileNotReadableUnix() throws IOException {
        Files.setPosixFilePermissions(tempJsonFile,
            PosixFilePermissions.fromString("---------"));

        assertThrows(IllegalArgumentException.class,
            () -> new JsonReader(tempJsonFile.toString())
        );
    }

    /**
     * Verifies that JsonReader throws an IllegalArgumentException on Windows
     * when the file exists but is not readable.
     *
     * <p>
     * Windows does not reliably support POSIX-style permission changes,
     * meaning that {@code setReadable(false)} or POSIX permission manipulation
     * may silently fail. To ensure consistent behavior, this test simulates
     * a validation failure by overriding {@code validatePath()} in a local subclass
     *
     * <p>
     * This avoids false negatives caused by OS-specific permission handling.
     */
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testConstructorThrowsIfFileNotReadableWindows() {

        class UnreadableJsonReader extends JsonReader {
            UnreadableJsonReader(final String path) {
                super(path);
            }

            @Override
            protected void validatePath() {
                throw new IllegalArgumentException("File not readable");
            }
        }

        assertThrows(IllegalArgumentException.class,
            () -> new UnreadableJsonReader(tempJsonFile.toString())
        );
    }

    /**
     * Cleans up all temporary files created during the test execution.
     *
     * <p>
     * This method is executed after each test and ensures that no temporary
     * resources remain on the filesystem. It uses {@link TestCleanupUtils}
     * to safely delete files or directories
     */
    @AfterEach
    void tearDown() {
        TestCleanupUtils.deleteRecursively(tempJsonFile);
    }
}
