package it.unibo.unibodget.persistency.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

/**
 * Utility class providing helper methods for cleaning up temporary files
 * and directories created during test execution. Useful in test suites 
 * that generate multiple temporary resources
 * 
 * Features:
 * - Deletes files safely without throwing exceptions
 * - Deletes directories recursively, including nested content
 * - Ignores missing paths to avoid unnecessary failures
 */
public final class TestCleanupUtils {

    /**
     * Private constructor to prevent instantiation.
     * This is a pure utility class.
     */
    private TestCleanupUtils() {
        // utility class
    }

    /**
     * Deletes a file or directory recursively if it exists.
     *
     * If the path refers to:
     * - a file      → it is deleted directly
     * - a directory → all its contents are deleted first, then the directory itself
     * 
     * @param path the file or directory to delete; may be null
     */
    public static void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }

        try {
            if (Files.isDirectory(path)) {
                // Walk the directory tree and delete children before parents
                Files.walk(path)
                     .sorted(Comparator.reverseOrder()) // Deepest paths first
                     .forEach(p -> {
                         try {
                             Files.deleteIfExists(p);
                         } catch (IOException ignored) {
                            // Clean up errors intentionally ignored to avoid test failures
                         }
                     });
            } else {
                Files.deleteIfExists(path); // Delete single file
            }
        } catch (IOException ignored) {
            // Intentionally ignore cleanup errors
        }
    }
}
