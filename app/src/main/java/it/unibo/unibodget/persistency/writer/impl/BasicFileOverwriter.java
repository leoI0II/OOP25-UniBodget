package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import it.unibo.unibodget.persistency.writer.api.FileOverwriter;

/**
 * Basic implementation of {@link FileOverwriter}.
 * Overwrites the file completely with the provided content.
 */
public class BasicFileOverwriter implements FileOverwriter {

    @Override
    public void overwrite(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
