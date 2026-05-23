package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import it.unibo.unibodget.persistency.writer.api.FileSaver;

/**
 * Basic implementation of {@link FileSaver}.
 * Saves content only if the file does not already exist.
 */
public class BasicFileSaver implements FileSaver {

    @Override
    public void save(Path path, String content) throws IOException {
        if (Files.exists(path)) {
            throw new IOException("File already exists: " + path);
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
