package it.unibo.unibodget.persistency.reader.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A FileReader implementation for reading JSON files.
 * 
 * <p>
 * This class extends {@link BasicReader} and provides a simple mechanism
 * to read the raw content of a JSON file as a {@code String}.
 * Implicit implements FileReader<String>
 */
public class JsonReader extends AbstractBasicReader<String> {

    /**
     * Creates a JSON reader for the specified file path.
     *
     * @param path the path of the JSON file to read
     */
    public JsonReader(final String path) {
        super(path);
    }

    /**
     * Reads the content of the JSON file associated with this reader.
     *
     * <p>
     * Subclasses overriding this method should either call
     * {@code super.readFile()} to preserve the default file‑reading behavior,
     * or explicitly document why the reading semantics are being changed.
     * Any override must maintain the contract of throwing an {@link IOException}
     * when the file cannot be read.
     * </p>
     *
     * @return the raw JSON content as a string
     * @throws IOException if the file cannot be read
     */
    @Override
    public String readFile() throws IOException {
        return Files.readString(Path.of(getPath()));
    }
}
