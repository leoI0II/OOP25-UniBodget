package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import it.unibo.unibodget.persistency.writer.api.FileAppender;

/**
 * JSON implementation of a {@link FileAppender}.
 * 
 * This appender ensures that the target file exists and then appends the given
 * content to it using {@link StandardOpenOption#APPEND}. No JSON validation is
 * performed because of the caller
 */
public class JsonFileAppender extends BasicFileAppender {

    /**
     * Appends the given content to the specified JSON file.
     * 
     * If the file does not exist, it is created along with any missing parent
     * directories. The content is then appended at the end of the file.
     * 
     * @param path    the file to append to
     * @param content the JSON content to append
     * @throws IOException if the file cannot be created or written
     */
    @Override
    public void append(Path path, String content) throws IOException {
        ensureFileExists(path);
        Files.writeString(path, content, StandardOpenOption.APPEND);
    }
}
