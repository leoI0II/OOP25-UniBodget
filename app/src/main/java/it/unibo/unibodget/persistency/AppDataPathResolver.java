package it.unibo.unibodget.persistency;

import com.fasterxml.jackson.databind.JsonNode;
import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resilient, centralized path resolver for locating and accessing the application data directory
 * and its persisted JSON and CSV resources.
 *
 * <p>The resolution follows a strict prioritized strategy:
 * <ol>
 *     <li><b>Step 1 (Local standard data directory):</b> If a {@code data/} directory exists in the
 *         current working directory, it is selected immediately with zero search overhead.</li>
 *     <li><b>Step 2 (Intelligent filesystem discovery):</b> If local {@code data/} is missing
 *         (e.g., launched from a subproject directory or moved), bounded search inspects adjacent
 *         paths and standard user directories, validating candidate directories by ensuring that
 *         {@code json/settings/Settings.json} exists and contains genuine UniBodget properties.</li>
 *     <li><b>Step 3 (Explicit runtime parameter):</b> Checks the optional JVM system property
 *         {@code unibodget.data.dir} or the environment variable {@code UNIBODGET_DATA_DIR}.</li>
 *     <li><b>Step 4 (Clean fallback):</b> Falls back to {@code Path.of("data")}, enabling
 *         {@link ModelFileManager} to initialize clean storage from bundled templates.</li>
 * </ol>
 */
public final class AppDataPathResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDataPathResolver.class);

    private static final String DATA_DIR_NAME = "data";
    private static final Path DEFAULT_DATA_DIR = Path.of(DATA_DIR_NAME);
    private static final String DATA_DIR_SYS_PROP = "unibodget.data.dir";
    private static final String DATA_DIR_ENV_VAR = "UNIBODGET_DATA_DIR";

    private static final String SETTINGS_RELATIVE_PATH = "json/settings/Settings.json";
    private static final String SETTINGS_KEY_BASE_CURRENCY = "baseCurrency";
    private static final String SETTINGS_KEY_THEME = "theme";
    private static final String SETTINGS_KEY_SETTINGS = "settings";

    private static final int MAX_SEARCH_DEPTH = 3;
    private static final int MAX_VISITED_DIRECTORIES = 500;

    private static volatile Path cachedDataDir;

    private AppDataPathResolver() {
        // utility class
    }

    /**
     * Returns the resolved base data directory, discovering it if not already cached.
     *
     * @return the resolved {@link Path} of the data directory
     */
    public static Path getDataDir() {
        Path result = cachedDataDir;
        if (result == null) {
            synchronized (AppDataPathResolver.class) {
                result = cachedDataDir;
                if (result == null) {
                    result = resolveBaseDataDir();
                    cachedDataDir = result;
                }
            }
        }
        return result;
    }

    /**
     * Resolves a relative path against the application data directory.
     *
     * @param relativePath the relative path to resolve
     * @return the resolved path
     */
    public static Path resolve(final String relativePath) {
        Objects.requireNonNull(relativePath, "relativePath must not be null");
        return getDataDir().resolve(normalizePrefix(relativePath));
    }

    /**
     * Resolves a relative path against the application data directory.
     *
     * @param relativePath the relative path to resolve
     * @return the resolved path
     */
    public static Path resolve(final Path relativePath) {
        Objects.requireNonNull(relativePath, "relativePath must not be null");
        return getDataDir().resolve(relativePath);
    }

    /**
     * Resolves a relative path under the {@code json/} directory inside the application data directory.
     *
     * <p>If the given relative path already starts with {@code "json/"} or {@code "json\\"}, it is
     * resolved directly under the data directory without duplication.
     *
     * @param relativeJsonPath the relative JSON file path (e.g. {@code "settings/Settings.json"})
     * @return the resolved {@link Path}
     */
    public static Path resolveJson(final String relativeJsonPath) {
        Objects.requireNonNull(relativeJsonPath, "relativeJsonPath must not be null");
        final String cleanPath = normalizePrefix(relativeJsonPath);
        if (cleanPath.startsWith("json/") || cleanPath.startsWith("json\\")) {
            return getDataDir().resolve(cleanPath);
        }
        return getDataDir().resolve("json").resolve(cleanPath);
    }

    /**
     * Resolves a relative path under the {@code csv/} directory inside the application data directory.
     *
     * <p>If the given relative path already starts with {@code "csv/"} or {@code "csv\\"}, it is
     * resolved directly under the data directory without duplication.
     *
     * @param relativeCsvPath the relative CSV file path (e.g. {@code "portfolio/transactions.csv"})
     * @return the resolved {@link Path}
     */
    public static Path resolveCsv(final String relativeCsvPath) {
        Objects.requireNonNull(relativeCsvPath, "relativeCsvPath must not be null");
        final String cleanPath = normalizePrefix(relativeCsvPath);
        if (cleanPath.startsWith("csv/") || cleanPath.startsWith("csv\\")) {
            return getDataDir().resolve(cleanPath);
        }
        return getDataDir().resolve("csv").resolve(cleanPath);
    }

    /**
     * Sets a custom data directory for testing or explicit configuration purposes.
     *
     * @param customDir the custom path to use
     */
    public static synchronized void setCustomDataDir(final Path customDir) {
        cachedDataDir = Objects.requireNonNull(customDir, "customDir must not be null");
        LOGGER.info("Application data directory manually set to: {}", cachedDataDir);
    }

    /**
     * Resets the cached resolved data directory, forcing re-resolution on subsequent calls.
     */
    public static synchronized void reset() {
        cachedDataDir = null;
    }

    /**
     * Executes the prioritized resolution strategy.
     *
     * @return the determined data directory
     */
    private static Path resolveBaseDataDir() {
        // Step 1: Standard local data directory (zero overhead, guaranteed stability)
        if (Files.isDirectory(DEFAULT_DATA_DIR)) {
            LOGGER.debug("Step 1: Found local data directory at {}", DEFAULT_DATA_DIR);
            return DEFAULT_DATA_DIR;
        }

        // Step 2: Intelligent filesystem discovery if local data directory does not exist
        final Path discovered = discoverExistingDataDir();
        if (discovered != null) {
            LOGGER.info("Step 2: Discovered existing UniBodget data directory at {}", discovered);
            return discovered;
        }

        // Step 3: Explicit configuration parameter (JVM system property or environment variable)
        final Path configured = resolveExplicitConfiguration();
        if (configured != null) {
            LOGGER.info("Step 3: Using explicitly configured data directory at {}", configured);
            return configured;
        }

        // Step 4: Clean fallback to default data directory, restored from bundled templates
        LOGGER.info("Step 4: Fallback to standard data directory {}", DEFAULT_DATA_DIR);
        return DEFAULT_DATA_DIR;
    }

    /**
     * Scans adjacent directories and standard user locations for an existing valid UniBodget data folder.
     *
     * @return the discovered path, or {@code null} if not found
     */
    private static Path discoverExistingDataDir() {
        // 2a. Immediate check of adjacent candidate paths
        final List<Path> adjacentCandidates = List.of(
                Path.of("..", DATA_DIR_NAME),
                Path.of("..", "..", DATA_DIR_NAME),
                Path.of("app", DATA_DIR_NAME),
                Path.of(System.getProperty("user.home", "."), ".unibodget", DATA_DIR_NAME)
        );

        for (final Path candidate : adjacentCandidates) {
            if (isValidUniBodgetDataDir(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }

        // 2b. Bounded scan in standard user directories
        final String userHome = System.getProperty("user.home", ".");
        final List<Path> searchRoots = List.of(
                Path.of(".."),
                Path.of(userHome, "Desktop"),
                Path.of(userHome, "Documents"),
                Path.of(userHome, "Downloads")
        );

        for (final Path root : searchRoots) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            final Path found = scanSubtree(root);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    /**
     * Scans a directory subtree with a bounded depth and directory count limit.
     *
     * @param root the search root directory
     * @return the valid UniBodget data directory if found, or {@code null}
     */
    private static Path scanSubtree(final Path root) {
        final AtomicReference<Path> found = new AtomicReference<>();
        final AtomicInteger visited = new AtomicInteger();

        try {
            Files.walkFileTree(root, Collections.emptySet(), MAX_SEARCH_DEPTH, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                    if (visited.incrementAndGet() > MAX_VISITED_DIRECTORIES) {
                        return FileVisitResult.TERMINATE;
                    }

                    final Path dirName = dir.getFileName();
                    if (dirName != null) {
                        final String name = dirName.toString();
                        if (name.startsWith(".")
                                || "build".equalsIgnoreCase(name)
                                || "out".equalsIgnoreCase(name)
                                || "bin".equalsIgnoreCase(name)
                                || "target".equalsIgnoreCase(name)
                                || "node_modules".equalsIgnoreCase(name)
                                || "Library".equalsIgnoreCase(name)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        if (DATA_DIR_NAME.equals(name) && isValidUniBodgetDataDir(dir)) {
                            found.set(dir.toAbsolutePath().normalize());
                            return FileVisitResult.TERMINATE;
                        }
                    }

                    final Path candidateSubData = dir.resolve(DATA_DIR_NAME);
                    if (isValidUniBodgetDataDir(candidateSubData)) {
                        found.set(candidateSubData.toAbsolutePath().normalize());
                        return FileVisitResult.TERMINATE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException | SecurityException ignored) {
            // Silently continue scanning other locations on permission errors
        }

        return found.get();
    }

    /**
     * Checks if a candidate directory is a valid UniBodget data directory.
     *
     * @param candidate the directory path to check
     * @return {@code true} if it contains valid UniBodget settings JSON, {@code false} otherwise
     */
    public static boolean isValidUniBodgetDataDir(final Path candidate) {
        if (candidate == null || !Files.isDirectory(candidate)) {
            return false;
        }

        final Path settingsFile = candidate.resolve(SETTINGS_RELATIVE_PATH);
        if (!Files.isRegularFile(settingsFile) || !Files.isReadable(settingsFile)) {
            return false;
        }

        try {
            final String raw = Files.readString(settingsFile).trim();
            if (raw.isEmpty()) {
                return false;
            }
            if (!raw.contains(SETTINGS_KEY_BASE_CURRENCY) && !raw.contains(SETTINGS_KEY_THEME)) {
                return false;
            }
            final JsonNode root = PersistenceJacksonConfig.mapper().readTree(raw);
            return root != null && (root.has(SETTINGS_KEY_BASE_CURRENCY)
                    || root.has(SETTINGS_KEY_THEME)
                    || root.has(SETTINGS_KEY_SETTINGS));
        } catch (final IOException | IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * Resolves the data directory from JVM system property or environment variable.
     *
     * @return the configured path, or {@code null} if neither was defined
     */
    private static Path resolveExplicitConfiguration() {
        final String sysProp = System.getProperty(DATA_DIR_SYS_PROP);
        if (sysProp != null && !sysProp.isBlank()) {
            return Path.of(sysProp.trim());
        }

        final String envVar = System.getenv(DATA_DIR_ENV_VAR);
        if (envVar != null && !envVar.isBlank()) {
            return Path.of(envVar.trim());
        }

        return null;
    }

    /**
     * Strips leading path separators from relative paths.
     *
     * @param path the path string
     * @return the normalized relative path string
     */
    private static String normalizePrefix(final String path) {
        if (path.startsWith("/") || path.startsWith("\\")) {
            return path.substring(1);
        }
        return path;
    }

}
