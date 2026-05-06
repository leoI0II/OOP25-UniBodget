package it.unibo.unibodget.persistency.writer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Appender for JSON files containing an array of objects.
 *
 * This implementation allows appending a new JSON object to the end of an
 * existing array stored in the file. If the file is empty, a valid empty array
 * is created automatically.
 * The appended content must be a valid JSON object, although no advanced
 * validation is performed.
 */
public class JsonObjectAppender extends BasicFileAppender {

    /**
     * Appends a new JSON object to the end of the array contained in the file.
     *
     * If the file is empty, it is initialized as an empty array
     * ({@code []}). If the file does not contain a valid JSON array,
     * an exception is thrown.
     *
     * @param path    the path to the JSON file
     * @param content the JSON object to append (as a string)
     * @throws IOException if the file does not contain a JSON array or cannot be written
     */
    @Override
    public void append(Path path, String content) throws IOException {
        ensureFileExists(path);

        String json = Files.readString(path).trim();

        if (json.isEmpty()) {
            json = "[]";
        }

        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IOException("The JSON file does not contain an array");
        }

        if (json.equals("[]")) {
            String result = "[\n" + indent(content) + "\n]";
            Files.writeString(path, JsonUtils.prettyPrint(result));
            return;
        }

        String body = json.substring(1, json.length() - 1).trim();

        String result =
            "[\n" +
            body +
            ",\n" +
            indent(content) +
            "\n]";

        Files.writeString(path, JsonUtils.prettyPrint(result));
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
