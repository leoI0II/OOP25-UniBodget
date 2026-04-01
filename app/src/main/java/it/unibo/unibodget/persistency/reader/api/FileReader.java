package it.unibo.unibodget.persistency.reader.api;

import java.io.IOException;

/**
 * An interface for reading the content of a file and converting it
 * into an object of type {@code T}.
 *
 * This abstraction allows different file formats (JSON, CSV, TXT, ...)
 * to be handled by providing specific implementations of this interface.
 * The caller only needs to specify the path of the file
 *
 * @param <T> the type of object returned after reading and parsing the file
 */
public interface FileReader<T> {
    /**
     * Read the content of a file given its path and return 
     * it into an object of type {@code T}.
     * If the file cannot be accessed or parsed, an {@link IOException} is thrown.
     * 
     * @param path the path to the file to be read
     * @return the parsed object of type {@code T}
     * @throws IOException if the file cannot be read
     */
    T readFile(String path) throws IOException;
}