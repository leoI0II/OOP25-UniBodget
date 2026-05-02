package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Generic JSON serializer that converts an object of type {@code T}
 * into a JSON string using reflection.
 *
 * Supported features:
 * - Records (canonical constructor fields)
 * - Standard classes (declared fields)
 * - Primitive types and wrappers
 * - Enums
 * - BigDecimal
 * - Lists
 * - Nested objects
 *
 * No external JSON libraries are used.
 *
 * @param <T> the type of object to serialize
 */
public class JsonDataSerializer<T> implements DataSerializer<T> {

    private final Class<T> type;

    /**
     * Creates a new serializer for the given type.
     *
     * @param type the class representing the type to serialize
     */
    public JsonDataSerializer(Class<T> type) {
        this.type = type;
    }

    /**
     * Serializes the given object into a JSON string.
     *
     * @param data the object to serialize
     * @return a JSON string representing the object
     * @throws DataSerializerException if serialization fails
     */
    @Override
    public String serialize(T data) throws DataSerializerException {
        try {
            return serializeValue(data, 0);
        } catch (Exception e) {
            throw new DataSerializerException(
                "Failed to serialize object of type " + type.getSimpleName(), e
            );
        }
    }

    /**
     * Serializes a value into JSON, dispatching based on its type.
     *
     * @param value  the value to serialize
     * @param indent the indentation level for pretty printing
     * @return a JSON string representing the value
     * @throws Exception if reflection fails
     */
    private String serializeValue(Object value, int indent) throws Exception {

        if (value == null) return "null";

        if (value instanceof String s) return "\"" + escape(s) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof BigDecimal bd) return bd.toString();

        if (value instanceof Enum<?> e) return "\"" + e.name() + "\"";

        if (value instanceof List<?> list) {
            return serializeList(list, indent);
        }

        return serializeObject(value, indent);
    }

    /**
     * Serializes a Java object (record or class) into a JSON object.
     *
     * @param obj    the object to serialize
     * @param indent the indentation level
     * @return a JSON object string
     * @throws Exception if reflection fails
     */
    private String serializeObject(Object obj, int indent) throws Exception {
        StringBuilder sb = new StringBuilder();
        String pad = " ".repeat(indent);

        sb.append("{\n");

        Class<?> clazz = obj.getClass();
        List<String> entries = new ArrayList<>();

        if (clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                Method accessor = rc.getAccessor();
                Object value = accessor.invoke(obj);
                entries.add(fieldEntry(rc.getName(), value, indent + 2));
            }
        } else {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(obj);
                entries.add(fieldEntry(field.getName(), value, indent + 2));
            }
        }

        sb.append(String.join(",\n", entries));
        sb.append("\n").append(pad).append("}");

        return sb.toString();
    }

    /**
     * Formats a single JSON field entry.
     *
     * @param name   the field name
     * @param value  the field value
     * @param indent the indentation level
     * @return a JSON field entry string
     * @throws Exception if serialization fails
     */
    private String fieldEntry(String name, Object value, int indent) throws Exception {
        String pad = " ".repeat(indent);
        return pad + "\"" + name + "\": " + serializeValue(value, indent);
    }

    /**
     * Serializes a Java List into a JSON array.
     *
     * @param list   the list to serialize
     * @param indent the indentation level
     * @return a JSON array string
     * @throws Exception if serialization fails
     */
    private String serializeList(List<?> list, int indent) throws Exception {
        StringBuilder sb = new StringBuilder();
        String pad = " ".repeat(indent);

        sb.append("[\n");

        List<String> entries = new ArrayList<>();
        for (Object elem : list) {
            entries.add(" ".repeat(indent + 2) + serializeValue(elem, indent + 2));
        }

        sb.append(String.join(",\n", entries));
        sb.append("\n").append(pad).append("]");

        return sb.toString();
    }

    /**
     * Escapes special characters inside JSON strings.
     *
     * @param s the raw string
     * @return the escaped string
     */
    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
