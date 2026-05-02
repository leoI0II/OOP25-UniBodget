package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Generic JSON parser that converts a JSON string into an instance of type {@code T}
 * using reflection. No external JSON libraries are used.
 *
 * The JSON text is first converted into a nested structure of:
 * - Map<String, Object> for JSON objects
 * - List<Object> for JSON arrays
 * - String, Number, Boolean, null for primitive values
 *
 * Then the structure is mapped to the target Java type using reflection.
 *
 * @param <T> the type of object produced by the parser
 */
public class JsonDataParser<T> implements DataParser<T> {

    private final Class<T> type;

    /**
     * Creates a new JSON parser for the given target type.
     *
     * @param type the class representing the target type
     */
    public JsonDataParser(Class<T> type) {
        this.type = type;
    }

    /**
     * Parses the given JSON string and converts it into an instance of {@code T}.
     *
     * @param data the JSON text to parse
     * @return a fully populated instance of {@code T}
     * @throws DataParserException if parsing fails or the JSON is malformed
     */
    @Override
    public T parse(String data) throws DataParserException {
        try {
            Object parsed = parseJson(data.trim());

            if (!(parsed instanceof Map<?, ?> rawMap)) {
                throw new DataParserException("Root JSON must be an object");
            }

            Map<String, Object> json = toStringObjectMap(rawMap);
            return parseObject(json, type);

        } catch (Exception e) {
            throw new DataParserException("Failed to parse JSON into " + type.getSimpleName(), e);
        }
    }

    /**
     * JSON TEXT → MAP/LIST/PRIMITIVES
     * Determines whether the JSON text represents an object or an array,
     * and delegates to the appropriate parsing method.
     *
     * @param json the raw JSON text
     * @return a parsed structure (Map, List, or primitive)
     */
    private Object parseJson(String json) {
        if (json.startsWith("{")) return parseObjectJson(json);
        if (json.startsWith("[")) return parseArrayJson(json);
        throw new IllegalArgumentException("Invalid JSON: " + json);
    }

    /**
     * Parses a JSON object into a Map<String, Object>.
     * Handles nested objects and arrays recursively.
     *
     * @param json the JSON object text
     * @return a Map representing the JSON object
     */
    private Map<String, Object> parseObjectJson(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        json = json.substring(1, json.length() - 1).trim(); // remove { }

        if (json.isEmpty()) return map;

        int i = 0;
        while (i < json.length()) {

            int keyStart = json.indexOf('"', i) + 1;
            int keyEnd = json.indexOf('"', keyStart);
            String key = json.substring(keyStart, keyEnd);

            int colon = json.indexOf(':', keyEnd);
            int[] range = parseValueRange(json, colon + 1);
            Object value = extractValue(json.substring(range[0], range[1]));

            map.put(key, value);
            i = range[1] + 1;

            if (i < json.length() && json.charAt(i) == ',') i++;
        }

        return map;
    }

    /**
     * Parses a JSON array into a List<Object>.
     * Handles nested arrays and objects recursively.
     *
     * @param json the JSON array text
     * @return a List representing the JSON array
     */
    private List<Object> parseArrayJson(String json) {
        List<Object> list = new ArrayList<>();
        json = json.substring(1, json.length() - 1).trim(); // remove [ ]

        if (json.isEmpty()) return list;

        int i = 0;
        while (i < json.length()) {
            int[] range = parseValueRange(json, i);
            Object value = extractValue(json.substring(range[0], range[1]));
            list.add(value);
            i = range[1] + 1;

            if (i < json.length() && json.charAt(i) == ',') i++;
        }

        return list;
    }

    /**
     * Identifies the boundaries of the next JSON value starting at index {@code start}.
     * Supports strings, objects, arrays, numbers, booleans, and null.
     *
     * @param json  the JSON text
     * @param start the starting index
     * @return an array [valueStart, valueEnd]
     */
    private int[] parseValueRange(String json, int start) {
        start = skipSpaces(json, start);
        int end = start;

        char c = json.charAt(start);

        if (c == '"') {
            end = json.indexOf('"', start + 1) + 1;
            return new int[]{start, end};
        }

        if (c == '{') {
            int depth = 1;
            end = start + 1;
            while (depth > 0) {
                if (json.charAt(end) == '{') depth++;
                if (json.charAt(end) == '}') depth--;
                end++;
            }
            return new int[]{start, end};
        }

        if (c == '[') {
            int depth = 1;
            end = start + 1;
            while (depth > 0) {
                if (json.charAt(end) == '[') depth++;
                if (json.charAt(end) == ']') depth--;
                end++;
            }
            return new int[]{start, end};
        }

        while (end < json.length() && ",]}".indexOf(json.charAt(end)) == -1) end++;
        return new int[]{start, end};
    }

    /**
     * Converts a raw JSON substring into a Java primitive, Map, List, or null.
     *
     * @param raw the raw JSON substring
     * @return the corresponding Java value
     */
    private Object extractValue(String raw) {
        raw = raw.trim();

        if (raw.startsWith("{")) return parseObjectJson(raw);
        if (raw.startsWith("[")) return parseArrayJson(raw);
        if (raw.startsWith("\"")) return raw.substring(1, raw.length() - 1);

        if (raw.equals("true")) return true;
        if (raw.equals("false")) return false;
        if (raw.equals("null")) return null;

        try {
            if (raw.contains(".")) return Double.parseDouble(raw);
            return Integer.parseInt(raw);
        } catch (Exception ignored) {}

        return raw;
    }

    /**
     * Skips whitespace characters starting from index {@code i}.
     *
     * @param s the string to scan
     * @param i the starting index
     * @return the next non-whitespace index
     */
    private int skipSpaces(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    /**
     * MAP → JAVA OBJECT (REFLECTION)
     * Converts a Map<String,Object> into an instance of the given class.
     * Supports both records and standard classes.
     *
     * @param json  the parsed JSON object
     * @param clazz the target class
     * @param <U>   the type of the resulting object
     * @return an instance of {@code U}
     * @throws Exception if reflection fails
     */
    private <U> U parseObject(Map<String, Object> json, Class<U> clazz) throws Exception {

        if (clazz.isRecord()) {
            return parseRecord(json, clazz);
        }

        U instance = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object raw = json.get(field.getName());
            Object value = convertValue(raw, field.getType(), field.getGenericType());
            field.set(instance, value);
        }

        return instance;
    }

    /**
     * Creates a record instance by invoking its canonical constructor.
     *
     * @param json  the parsed JSON object
     * @param clazz the record class
     * @param <U>   the record type
     * @return a new record instance
     * @throws Exception if reflection fails
     */
    private <U> U parseRecord(Map<String, Object> json, Class<U> clazz) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();
        Object[] args = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent rc = components[i];
            Object raw = json.get(rc.getName());
            args[i] = convertValue(raw, rc.getType(), rc.getGenericType());
        }

        Constructor<U> ctor = clazz.getDeclaredConstructor(
                Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new)
        );

        return ctor.newInstance(args);
    }

    /**
     * Converts a raw JSON value into the target Java type.
     *
     * @param raw         the raw JSON value
     * @param targetType  the expected Java type
     * @param genericType the generic type (for lists)
     * @return the converted Java value
     * @throws Exception if conversion fails
     */
    private Object convertValue(Object raw, Class<?> targetType, Type genericType) throws Exception {

        if (raw == null) return null;

        if (targetType == String.class) return raw.toString();
        if (targetType == int.class || targetType == Integer.class) return ((Number) raw).intValue();
        if (targetType == double.class || targetType == Double.class) return ((Number) raw).doubleValue();
        if (targetType == boolean.class || targetType == Boolean.class) return raw;

        if (targetType == BigDecimal.class) return new BigDecimal(raw.toString());

        if (targetType.isEnum()) {
            return parseEnum(targetType, raw);
        }

        if (raw instanceof Map<?, ?> map) {
            return parseObject(toStringObjectMap(map), targetType);
        }

        if (raw instanceof List<?> list && targetType == List.class) {
            return parseList(list, genericType);
        }

        return raw;
    }

    /**
     * Converts a raw JSON array into a Java List with the correct element type.
     *
     * @param rawList     the raw JSON list
     * @param genericType the generic type information
     * @return a Java List containing converted elements
     * @throws Exception if conversion fails
     */
    private List<?> parseList(List<?> rawList, Type genericType) throws Exception {
        List<Object> list = new ArrayList<>();

        Class<?> elementType = Object.class;

        if (genericType instanceof ParameterizedType pt) {
            elementType = (Class<?>) pt.getActualTypeArguments()[0];
        }

        for (Object raw : rawList) {
            list.add(convertValue(raw, elementType, elementType));
        }

        return list;
    }

    /**
     * Converts a raw map into a Map<String,Object>, ensuring all keys are strings.
     *
     * @param raw the raw map
     * @return a type-safe Map<String,Object>
     */
    private Map<String, Object> toStringObjectMap(Map<?, ?> raw) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            Object key = entry.getKey();
            if (!(key instanceof String)) {
                throw new IllegalArgumentException("JSON object contains non-string key: " + key);
            }
            result.put((String) key, entry.getValue());
        }
        return result;
    }

    /**
     * Converts a raw JSON value into an enum constant of the target enum type.
     * This method does not use Enum.valueOf to avoid generic type issues.
     *
     * @param targetType the enum class (as Class<?>)
     * @param raw        the raw JSON value
     * @return the enum constant
     */
    private Object parseEnum(Class<?> targetType, Object raw) {
        Object[] constants = targetType.getEnumConstants();
        if (constants == null) {
            throw new IllegalArgumentException("Type is not an enum: " + targetType);
        }
        String name = raw.toString();
        for (Object constant : constants) {
            Enum<?> e = (Enum<?>) constant;
            if (e.name().equals(name)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
            "No enum constant " + targetType.getName() + "." + name
        );
    }
}
