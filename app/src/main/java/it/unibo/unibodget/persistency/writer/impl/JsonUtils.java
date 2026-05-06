package it.unibo.unibodget.persistency.writer.impl;

/**
 * Utility class providing minimal JSON support
 *
 * This class is intentionally lightweight and designed specifically for the
 * simple JSON structures used in UniBodget's persistence layer. It does not
 * implement a full JSON parser and should not be used for complex or malformed
 * JSON inputs.
 *
 * Supported formatting:
 * - Arrays of flat JSON objects (e.g. {@code [{...}, {...}]})
 * - Objects containing nested arrays (e.g. a wallet with a {@code history} list)
 */
public final class JsonUtils {

    /** Prevents instantiation of this utility class. */
    private JsonUtils() {}

    /**
     * Pretty-prints a JSON array containing one or more JSON objects.
     * Reformats the array with indentation and line breaks, making it
     * easier to read.
     * 
     * @param json the raw JSON array string
     * @return a human-readable, indented version of the array
     */
    public static String prettyPrintArray(String json) {
        json = json.trim();
        if (json.equals("[]")) return "[\n]";

        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        String body = json.substring(1, json.length() - 1).trim();
        String[] elements = body.split("},\\s*\\{");

        for (int i = 0; i < elements.length; i++) {
            String e = elements[i];

            if (!e.startsWith("{")) e = "{" + e;
            if (!e.endsWith("}")) e = e + "}";

            sb.append("    ").append(e);

            if (i < elements.length - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Pretty-prints a JSON object, including nested arrays.
     * This method walks through the JSON character by character and inserts
     * indentation based on braces and brackets.
     *
     * @param json the raw JSON object string
     * @return a formatted, indented version of the JSON object
     */
    public static String prettyPrint(String json) {
        json = json.trim();
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inString = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            }

            if (inString) {
                sb.append(c);
                continue;
            }

            switch (c) {
                case '{':
                case '[':
                    sb.append(c).append("\n");
                    indent++;
                    appendIndent(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append("\n");
                    indent--;
                    appendIndent(sb, indent);
                    sb.append(c);
                    break;
                case ',':
                    sb.append(",\n");
                    appendIndent(sb, indent);
                    break;
                case ':':
                    sb.append(": ");
                    break;
                default:
                    if (!Character.isWhitespace(c)) {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Appends indentation spaces to the given {@link StringBuilder}.
     *
     * @param sb the builder to append to
     * @param indent the indentation level (each level = 4 spaces)
     */
    private static void appendIndent(StringBuilder sb, int indent) {
        sb.append("    ".repeat(Math.max(0, indent)));
    }
}
