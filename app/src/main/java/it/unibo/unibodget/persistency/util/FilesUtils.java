package it.unibo.unibodget.persistency.util;

/**
 * A utility class providing common file‑related helper methods used across the
 * persistency module.
 */
public class FilesUtils {

    /**
     * Private constructor to prevent instantiation.
     * This is a pure utility class.
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
}
