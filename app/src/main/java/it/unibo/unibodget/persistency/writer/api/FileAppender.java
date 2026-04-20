package it.unibo.unibodget.persistency.writer.api;

import java.io.IOException;
import java.nio.file.Path;

public interface FileAppender {
    void append(Path path, String content) throws IOException;
}
