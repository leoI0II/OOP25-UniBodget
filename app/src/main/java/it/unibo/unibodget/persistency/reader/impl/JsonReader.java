package it.unibo.unibodget.persistency.reader.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A FileReader implementation for reading JSON files.
 * 
 * This class extends {@link BasicReader} and provides a simple mechanism
 * to read the raw content of a JSON file as a {@code String}.
 * Implicit implements FileReader<String>
 */
public class JsonReader extends BasicReader<String> {

    public JsonReader(final String path) {
        super(path);
    }

    @Override
    public String readFile() throws IOException {
        return Files.readString(Path.of(getPath()));
    }
}