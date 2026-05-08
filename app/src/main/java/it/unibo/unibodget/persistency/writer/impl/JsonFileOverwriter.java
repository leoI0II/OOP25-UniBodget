package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import it.unibo.unibodget.persistency.writer.api.FileOverwriter;

/**
 * JSON-specific implementation of {@link FileOverwriter}.
 *
 * <p>This class overwrites the entire content of a JSON file. If the file or
 * its parent directories do not exist, they are created automatically.</p>
 *
 * <p>No JSON validation is performed. The caller is responsible for providing
 * valid JSON content.</p>
 */
public class JsonFileOverwriter extends BasicFileOverwriter {

    /**
     * Overwrites the specified JSON file with the given content.
     *
     * @param path    the JSON file to overwrite
     * @param content the new JSON content
     * @throws IOException if the file cannot be created or written
     */
    @Override
    public void overwrite(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
