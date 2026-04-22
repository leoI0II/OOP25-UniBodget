package it.unibo.unibodget.persistency.reader.impl;

import it.unibo.unibodget.persistency.reader.api.FileReader;

/**
 * A factory class responsible for creating the appropriate {@link FileReader}
 * implementation based on the file extension of the provided path.
 * 
 * Currently supports JSON files, but can be easily extended to support other formats.
 * This class is declared final because it is not intended to be extended,
 * and it has a private constructor because it only provides static factory methods.
 */
public final class FileReaderFactory {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class containing only static methods.
     */
    private FileReaderFactory() {
        // utility class
    }

    /**
     * Creates a {@link FileReader} capable of reading the file located at the given path.
     *
     * @param path the path of the file to read
     * @return a {@link FileReader} implementation suitable for the file type
     * @throws IllegalArgumentException if the file extension is missing or unsupported
     */
    public static FileReader<?> create(final String path) {
        return switch (getFileExtension(path)) {
            case "json" -> new JsonReader(path);
            default -> throw new IllegalArgumentException("Unsupported file type: " + getFileExtension(path));
        };
    }

    /**
     * Extracts the file extension from a file path.
     * 
     * @param path
     * @return the lowercase file extension (e.g., "json")
     * @throws IllegalArgumentException if the path has no valid extension
     */
    private static String getFileExtension(String path) {
        final int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == path.length() - 1) {
            throw new IllegalArgumentException("File path must have an extension: " + path);
        }
        return path.substring(lastDotIndex + 1).toLowerCase();
    }
}