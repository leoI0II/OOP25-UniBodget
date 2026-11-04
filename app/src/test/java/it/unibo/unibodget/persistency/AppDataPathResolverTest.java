package it.unibo.unibodget.persistency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link AppDataPathResolver}.
 */
class AppDataPathResolverTest {

    private static final String CUSTOM_TXT = "custom.txt";
    private static final String SETTINGS_JSON_NAME = "settings/Settings.json";
    private static final String JSON_DIR_NAME = "json";
    private static final String TRANSACTIONS_CSV_NAME = "portfolio/transactions.csv";

    @BeforeEach
    void setUp() {
        AppDataPathResolver.reset();
    }

    @AfterEach
    void tearDown() {
        AppDataPathResolver.reset();
        System.clearProperty("unibodget.data.dir");
    }

    @Test
    void shouldReturnDefaultOrDiscoveredDataDir() {
        final Path dataDir = AppDataPathResolver.getDataDir();
        assertNotNull(dataDir);
        assertTrue(Files.exists(dataDir) || dataDir.endsWith(Path.of("data")));
    }

    @Test
    void shouldResolvePathsCorrectly() {
        final Path base = AppDataPathResolver.getDataDir();

        final Path direct = AppDataPathResolver.resolve(CUSTOM_TXT);
        assertEquals(base.resolve(CUSTOM_TXT), direct);

        final Path directFromPath = AppDataPathResolver.resolve(Path.of(CUSTOM_TXT));
        assertEquals(base.resolve(CUSTOM_TXT), directFromPath);

        final Path jsonPath = AppDataPathResolver.resolveJson(SETTINGS_JSON_NAME);
        assertEquals(base.resolve(JSON_DIR_NAME).resolve(SETTINGS_JSON_NAME), jsonPath);

        final Path jsonPathPrefixed = AppDataPathResolver.resolveJson("json/" + SETTINGS_JSON_NAME);
        assertEquals(base.resolve("json/" + SETTINGS_JSON_NAME), jsonPathPrefixed);

        final Path jsonPathSlash = AppDataPathResolver.resolveJson("/" + SETTINGS_JSON_NAME);
        assertEquals(base.resolve(JSON_DIR_NAME).resolve(SETTINGS_JSON_NAME), jsonPathSlash);

        final Path csvPath = AppDataPathResolver.resolveCsv(TRANSACTIONS_CSV_NAME);
        assertEquals(base.resolve("csv").resolve(TRANSACTIONS_CSV_NAME), csvPath);

        final Path csvPathPrefixed = AppDataPathResolver.resolveCsv("csv/" + TRANSACTIONS_CSV_NAME);
        assertEquals(base.resolve("csv/" + TRANSACTIONS_CSV_NAME), csvPathPrefixed);

        final Path csvPathSlash = AppDataPathResolver.resolveCsv("/" + TRANSACTIONS_CSV_NAME);
        assertEquals(base.resolve("csv").resolve(TRANSACTIONS_CSV_NAME), csvPathSlash);
    }

    @Test
    void shouldSupportCustomDataDirAndReset(@TempDir final Path tempDir) {
        AppDataPathResolver.setCustomDataDir(tempDir);
        assertEquals(tempDir, AppDataPathResolver.getDataDir());

        final Path resolved = AppDataPathResolver.resolveJson("test.json");
        assertEquals(tempDir.resolve(JSON_DIR_NAME).resolve("test.json"), resolved);

        AppDataPathResolver.reset();
        final Path recomputed = AppDataPathResolver.getDataDir();
        assertNotNull(recomputed);
    }

    @Test
    void shouldValidateUniBodgetDataDirProperly(@TempDir final Path tempDir) throws IOException {
        // Non-existent directory
        assertFalse(AppDataPathResolver.isValidUniBodgetDataDir(tempDir.resolve("non_existent")));

        // Empty directory
        assertFalse(AppDataPathResolver.isValidUniBodgetDataDir(tempDir));

        final Path settingsDir = tempDir.resolve(JSON_DIR_NAME).resolve("settings");
        Files.createDirectories(settingsDir);
        final Path settingsFile = settingsDir.resolve("Settings.json");

        // Invalid JSON file
        Files.writeString(settingsFile, "invalid json");
        assertFalse(AppDataPathResolver.isValidUniBodgetDataDir(tempDir));

        // Valid JSON but unrelated content
        Files.writeString(settingsFile, "{\"name\": \"other_app\"}");
        assertFalse(AppDataPathResolver.isValidUniBodgetDataDir(tempDir));

        // Valid UniBodget settings JSON
        final String validJson = """
                {
                    "theme": { "name": "Dark", "darkMode": true },
                    "baseCurrency": "EUR"
                }
                """;
        Files.writeString(settingsFile, validJson);
        assertTrue(AppDataPathResolver.isValidUniBodgetDataDir(tempDir));
    }

}
