package it.unibo.unibodget.persistency.parser.impl;

import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * JSON serializer implementation used by UniBodget to convert domain model
 * objects into JSON strings without relying on external libraries.
 *
 * This serializer supports:
 * - Primitive types and strings
 * - Enums
 * - {@link LocalDate}
 * - {@link BigDecimal}
 * - Java records
 * - POJOs with inheritance
 * - Lists and maps
 *
 * To prevent infinite recursion, circular references are detected using an
 * {@link IdentityHashMap}. When a cycle is found, the value is replaced with
 * the literal {@code "<circular>"}.
 *
 * Static fields are ignored during serialization, ensuring that constant
 * instances (e.g., predefined categories) do not pollute the JSON output.
 *
 * @param <T> the type of the root object to serialize
 */
public final class JsonDataSerializer<T> implements DataSerializer<T> {

    /**
     * Creates a new serializer instance.
     *
     * The {@code rootType} parameter is kept for API compatibility but is
     * not required by this implementation.
     *
     * @param rootType the type of objects this serializer handles
     */
    public JsonDataSerializer(Class<T> rootType) {
        // No initialization needed for this implementation
    }

    /**
     * Serializes the given value into a JSON string.
     *
     * @param value the object to serialize
     * @return the JSON representation of the object
     * @throws DataSerializerException if serialization fails
     */
    @Override
    public String serialize(T value) throws DataSerializerException {
        try {
            return serializeValue(value, new IdentityHashMap<>());
        } catch (Exception e) {
            throw new DataSerializerException("Serialization failed", e);
        }
    }

    /**
     * Dispatches serialization based on the runtime type of the value.
     *
     * @param value the value to serialize
     * @param visited objects already processed (for circular reference detection)
     * @return the JSON representation of the value
     */
    private String serializeValue(Object value, Map<Object, Boolean> visited) throws Exception {
        if (value == null) return "null";
        if (value instanceof String s) return "\"" + escape(s) + "\"";
        if (value instanceof Number n) return n.toString();
        if (value instanceof Boolean b) return b.toString();
        if (value instanceof Enum<?> e) return "\"" + e.name() + "\"";
        if (value instanceof LocalDate d) return "\"" + d.toString() + "\"";
        if (value instanceof BigDecimal bd) return bd.toPlainString();
        if (value instanceof List<?> list) return serializeList(list, visited);
        if (value.getClass().isRecord()) return serializeRecord(value, visited);
        if (value instanceof Map<?, ?> map) return serializeMap(map, visited);

        return serializeObject(value, visited);
    }

    /**
     * Serializes a {@link List} into a JSON array.
     *
     * @param list the list to serialize
     * @param visited circular reference tracker
     * @return JSON array string
     */
    private String serializeList(List<?> list, Map<Object, Boolean> visited) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (Object elem : list) {
            if (!first) sb.append(",");
            sb.append(serializeValue(elem, visited));
            first = false;
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Serializes a {@link Map} into a JSON object.
     *
     * @param map the map to serialize
     * @param visited circular reference tracker
     * @return JSON object string
     */
    private String serializeMap(Map<?, ?> map, Map<Object, Boolean> visited) throws Exception {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (var entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escape(entry.getKey().toString())).append("\":");
            sb.append(serializeValue(entry.getValue(), visited));
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializes a Java record by iterating over its components.
     *
     * @param record the record instance
     * @param visited circular reference tracker
     * @return JSON object string
     */
    private String serializeRecord(Object record, Map<Object, Boolean> visited) throws Exception {
        if (visited.containsKey(record)) return "\"<circular>\"";
        visited.put(record, true);

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (RecordComponent comp : record.getClass().getRecordComponents()) {
            if (!first) sb.append(",");
            sb.append("\"").append(comp.getName()).append("\":");

            Object fieldValue = comp.getAccessor().invoke(record);
            sb.append(serializeValue(fieldValue, visited));

            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializes a POJO by reflecting over its fields, including inherited ones.
     * Static fields are ignored. Circular references are detected.
     *
     * @param obj the object to serialize
     * @param visited circular reference tracker
     * @return JSON object string
     */
    private String serializeObject(Object obj, Map<Object, Boolean> visited) throws Exception {
        if (visited.containsKey(obj)) return "\"<circular>\"";
        visited.put(obj, true);

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        Class<?> cls = obj.getClass();
        while (cls != null && cls != Object.class) {

            for (Field field : cls.getDeclaredFields()) {

                // Skip static fields (e.g., predefined Category constants)
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);

                if (!first) sb.append(",");
                sb.append("\"").append(field.getName()).append("\":");

                Object fieldValue = field.get(obj);
                sb.append(serializeValue(fieldValue, visited));

                first = false;
            }

            cls = cls.getSuperclass();
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Escapes double quotes inside strings to produce valid JSON.
     *
     * @param s the string to escape
     * @return the escaped string
     */
    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
