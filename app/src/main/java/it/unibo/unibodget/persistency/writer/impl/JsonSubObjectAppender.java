package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Appender for adding a JSON object inside a list contained within a parent
 * JSON object.
 *
 * This class allows appending an element to an internal list identified
 * by a key (e.g., {@code "history"}).
 * The appended content must be a valid JSON object. No advanced syntax
 * validation is performed.
 */
public class JsonSubObjectAppender extends BasicFileAppender {

    private final String listKey;

    /**
     * Creates an appender for a specific internal JSON list.
     *
     * @param listKey the name of the JSON list to modify
     */
    public JsonSubObjectAppender(String listKey) {
        this.listKey = listKey;
    }

    /**
     * Appends a JSON object to the internal list identified by {@code listKey}.
     *
     * The file must contain a valid JSON object with a list under the
     * specified key. If the key does not exist or does not contain an array,
     * an exception is thrown.
     *
     * @param path    the path to the JSON file
     * @param content the JSON object to append (as a string)
     * @throws IOException if the JSON structure is invalid or cannot be written
     */
    @Override
    public void append(Path path, String content) throws IOException {
        ensureFileExists(path);

        String json = Files.readString(path).trim();

        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IOException("The JSON file does not contain an object");
        }

        String keyPattern = "\"" + listKey + "\"";
        int keyIndex = json.indexOf(keyPattern);

        if (keyIndex < 0) {
            throw new IOException("Key '" + listKey + "' not found in JSON");
        }

        int arrayStart = json.indexOf("[", keyIndex);
        int arrayEnd   = json.indexOf("]", arrayStart);

        if (arrayStart < 0 || arrayEnd < 0) {
            throw new IOException("Key '" + listKey + "' does not contain an array");
        }

        String arrayContent = json.substring(arrayStart + 1, arrayEnd).trim();

        String newArray;
        if (arrayContent.isEmpty()) {
            newArray = indent(content);
        } else {
            newArray = arrayContent + ",\n" + indent(content);
        }

        String result =
            json.substring(0, arrayStart + 1) +
            "\n" + newArray + "\n" +
            json.substring(arrayEnd);

        Files.writeString(path, result);
    }

    /**
     * Indents a JSON string using 4 spaces.
     *
     * @param s the string to indent
     * @return the indented string
     */
    private String indent(String s) {
        return "    " + s.replace("\n", "\n    ");
    }
}
