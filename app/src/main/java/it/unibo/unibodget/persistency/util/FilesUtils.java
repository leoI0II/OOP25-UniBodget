package it.unibo.unibodget.persistency.util;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A utility class providing common file‑related helper methods used across the
 * persistency module.
 */
public class FilesUtils {

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();
    // private static final Path APP_ROOT = PROJECT_ROOT.resolve("app");

    /**
     * Private constructor to prevent instantiation.
     */
    private FilesUtils() {
        // utility class
    }

    /**
     * Extracts the file extension from a file path.
     * 
     * @param path
     * @return the lowercase file extension
     * @throws IllegalArgumentException if the path has no valid extension
     */
    public static String getFileExtension(String path) {
        final int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == path.length() - 1) {
            throw new IllegalArgumentException("File path must have an extension: " + path);
        }
        return path.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Searches recursively for a file with the given name inside the
     * application resources directory.
     *
     * @param fileName the name of the file to search for
     * @return the path to the file, or null if not found
     */
    public static Path findFileByName(String fileName) {
        // Defines the root directory where the search begins
        Path root = PROJECT_ROOT.resolve("app/src/main/resources");

        try {
            // Walks the directory tree and returns the first file whose name matches the requested
            return Files.walk(root)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error searching for file: " + e.getMessage());
            // Returns null if an I/O error occurs or the directory cannot be scanned
            return null;
        }
    }
}
