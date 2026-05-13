package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A lightweight JSON parser that maps simple JSON objects and arrays
 * to Java objects using reflection. This parser supports flat JSON
 * structures and basic value types such as strings, numbers, booleans
 * and enums.
 *
 * @param <T> the target type to be created from the parsed JSON
 */
public final class JsonDataParser<T> implements DataParser<T> {

    private final Class<T> targetClass;

    /**
     * Creates a new parser for the given target class.
     *
     * @param targetClass the class that JSON objects will be mapped to
     */
    public JsonDataParser(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Parses a JSON object and converts it into an instance of the target class.
     * The JSON must represent a single object with simple key-value pairs.
     *
     * @param json                  the JSON object as a string
     * @return                      an instance of the target class populated with parsed values
     * @throws DataParserException  if the JSON is malformed or cannot be mapped
     */
    @Override
    public T parse(String json) throws DataParserException {
        try {
            Map<String, Object> map = parseJsonObject(json);
            return createObjectFromMap(map, targetClass);
        } catch (Exception e) {
            throw new DataParserException("Error parsing JSON object: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a JSON object into a map of key-value pairs.
     * Only flat objects are supported. Nested structures are not handled.
     *
     * @param json the JSON object as a string
     * @return     a map containing the parsed key-value pairs
     */
    private Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> map = new HashMap<>();

        // Removes surrounding braces and trims whitespace
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        // Splits the object into individual key-value entries
        String[] entries = json.split(",");
        // Processes each entry and extracts the key and value
        for (String entry : entries) {
            String[] kv = entry.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim().replace("\"", "");
            String value = kv[1].trim().replace("\"", "");
            map.put(key, value);
        }
        return map;
    }

    /**
     * Creates an instance of the target class and assigns values to its fields
     * based on the provided map. Field names must match JSON keys.
     *
     * @param map           a map containing JSON key-value pairs
     * @param clazz         the class to instantiate
     * @return              a populated instance of the target class
     * @throws Exception    if reflection fails or a field cannot be assigned
     */
    private T createObjectFromMap(Map<String, Object> map, Class<T> clazz)
            throws Exception {
        // Creates a new instance using the default constructor
        T instance = clazz.getDeclaredConstructor().newInstance();
        // Iterates over all declared fields and assigns matching values
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object rawValue = map.get(field.getName());
            if (rawValue == null) continue;
            // Converts the raw string value into the correct Java type
            Object value = convertValue(rawValue, field.getType());
            field.set(instance, value);
        }
        return instance;
    }

    /**
     * Converts a raw JSON value into the expected Java type.
     * Supports strings, integers, doubles, booleans and enums.
     *
     * @param raw       the raw value extracted from JSON
     * @param type      the expected target type
     * @return          the converted value, or null if the type is unsupported
     */
    private Object convertValue(Object raw, Class<?> type) {
        String value = raw.toString();
        // Basic type conversions
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        // Enum conversion
        if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, value);
        }
        // Unsupported type
        return null;
    }

    /**
     * Parses a JSON array containing multiple objects and converts each
     * element into an instance of the target class.
     *
     * @param jsonArray             the JSON array as a string
     * @return                      a list of parsed objects
     * @throws DataParserException  if the array is malformed or parsing fails
     */
    public List<T> parseList(String jsonArray) throws DataParserException {
        try {
            List<T> result = new ArrayList<>();
            jsonArray = jsonArray.trim();
            // Ensures the string is a valid JSON array
            if (!jsonArray.startsWith("[") || !jsonArray.endsWith("]")) {
                throw new DataParserException("Invalid JSON array: " + jsonArray);
            }
            // Removes the surrounding brackets
            jsonArray = jsonArray.substring(1, jsonArray.length() - 1).trim();
            if (jsonArray.isEmpty()) {
                return result; // empty array
            }
            // Splits the array into individual JSON objects
            String[] objects = jsonArray.split("\\},\\s*\\{");
            // Parses each object separately
            for (String obj : objects) {
                String jsonObject = obj.trim();
                if (!jsonObject.startsWith("{")) jsonObject = "{" + jsonObject;
                if (!jsonObject.endsWith("}")) jsonObject = jsonObject + "}";
                result.add(parse(jsonObject));
            }
            return result;
        } catch (Exception e) {
            throw new DataParserException("Error parsing JSON array: " + e.getMessage(), e);
        }
    }

    /**
     * Reads a JSON file, extracts the array associated with the given key,
     * and parses each element into an instance of the target class.
     *
     * @param file                  the path to the JSON file
     * @param arrayKey              the key identifying the JSON array to extract
     * @return                      a list of parsed objects
     * @throws DataParserException  if the file cannot be read, the key is missing,
     *                              or the array is malformed
     */
    public List<T> parseListFromFile(Path file, String arrayKey) throws DataParserException {
        try {
            // Reads the entire file content as a string
            String json = Files.readString(file).trim();
            // Locates the key associated with the array and extracts the array content
            int keyIndex = json.indexOf("\"" + arrayKey + "\"");
            if (keyIndex == -1) throw new DataParserException("Key '" + arrayKey + "' not found");
            // Extracts the array boundaries
            int start = json.indexOf("[", keyIndex);
            int end = json.indexOf("]", start);
            if (start == -1 || end == -1){
                throw new DataParserException("Array '" + arrayKey + "' malformed");
            }
            String arrayContent = json.substring(start, end + 1);
            // Parses the extracted array
            return parseList(arrayContent);
        } catch (IOException e) {
            throw new DataParserException("Cannot read file: " + e.getMessage(), e);
        }
    }

    /**
     * Reads a JSON file from the given path and parses it as a JSON array
     * into a list of objects of type T. This method delegates file reading
     * to JsonReader and JSON parsing to parseList(String), keeping the
     * parsing workflow fully generic and independent of domain logic.
     *
     * @param file                  the path to the JSON file to load
     * @return                      a list of parsed objects of type T
     * @throws DataParserException  if the file cannot be read or the JSON content is invalid
     * @throws IOException          if an I/O error occurs while reading the file
     */
    public List<T> loadListFromFile(Path file) {
        try {
            String json = Files.readString(file);
            return parseList(json);
        } catch (DataParserException e) {
            System.err.println("Error parsing JSON from file: " + e.getMessage());
            return new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Cannot read file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
