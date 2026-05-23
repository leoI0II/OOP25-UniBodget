package it.unibo.unibodget.persistency.serializer;

import it.unibo.unibodget.persistency.parser.api.DataSerializer;
import it.unibo.unibodget.persistency.parser.api.DataSerializerException;
import it.unibo.unibodget.persistency.parser.impl.JsonDataSerializer;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.FiatCurrency;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryType;

import it.unibo.unibodget.model.transactions.base.CashTransaction;

import it.unibo.unibodget.model.utils.ARGBColor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsonDataSerializer}.
 * 
 * These tests verify that the serializer correctly converts domain model
 * objects into JSON strings. The goal is to ensure that primitive values,
 * records, POJOs, lists, and nested structures are serialized in a format
 * compatible with the application's persistence layer.
 */
public final class JsonDataSerializerTest {

    /**
     * Tests serialization of a simple {@link Asset} instance.
     * 
     * Verifies that both the currency (enum) and the amount (BigDecimal)
     * are correctly represented in the resulting JSON string.
     */
    @Test
    @DisplayName("Serialize Asset")
    void testSerializeAsset() throws DataSerializerException {
        Asset asset = Asset.of(FiatCurrency.EUR, new BigDecimal("123.45"));
        DataSerializer<Asset> serializer = new JsonDataSerializer<>(Asset.class);

        String json = serializer.serialize(asset);

        assertTrue(json.contains("\"currency\":\"EUR\""));
        assertTrue(json.contains("\"amount\":123.45"));
    }

    /**
     * Tests serialization of an {@link Asset} with zero amount.
     * 
     * Ensures that numeric values equal to zero are serialized without
     * quotes and without losing precision.
     */
    @Test
    @DisplayName("Serialize zero Asset")
    void testSerializeZeroAsset() throws DataSerializerException {
        Asset asset = Asset.zero(FiatCurrency.USD);
        DataSerializer<Asset> serializer = new JsonDataSerializer<>(Asset.class);

        String json = serializer.serialize(asset);

        assertTrue(json.contains("\"currency\":\"USD\""));
        assertTrue(json.contains("\"amount\":0"));
    }

    /**
     * Tests serialization of a list of {@link Asset} objects.
     * 
     * Verifies that lists are serialized as JSON arrays and that each
     * element is serialized consistently with the rules applied to single
     * Asset instances.
     */
    @Test
    @DisplayName("Serialize List<Asset>")
    void testSerializeAssetList() throws DataSerializerException {
        List<Asset> list = List.of(
                Asset.of(FiatCurrency.USD, BigDecimal.TEN),
                Asset.of(FiatCurrency.EUR, new BigDecimal("20"))
        );

        DataSerializer<List> serializer = new JsonDataSerializer<>(List.class);
        String json = serializer.serialize(list);

        assertTrue(json.contains("\"currency\":\"USD\""));
        assertTrue(json.contains("\"currency\":\"EUR\""));
    }

    /**
     * Tests serialization of a complete {@link CashTransaction}.
     * 
     * This test checks correct serialization of nested objects:
     * - {@link Asset}
     * - {@link Category}
     * - {@link LocalDate}
     * - String fields (description, notes)
     * 
     * It ensures that the serializer handles composition and inheritance
     * without producing circular references or invalid JSON.
     */
    @Test
    @DisplayName("Serialize CashTransaction")
    void testSerializeCashTransaction() throws DataSerializerException {

        Asset asset = Asset.of(FiatCurrency.USD, new BigDecimal("50"));

        Category category = new Category(
                "Food",
                new ARGBColor(0xFFFF0000),
                CategoryType.EXPENSE
        );

        CashTransaction tx = new CashTransaction(
                asset,
                category,
                LocalDate.of(2024, 1, 1),
                "Lunch",
                "Paid in cash"
        );

        DataSerializer<CashTransaction> serializer = new JsonDataSerializer<>(CashTransaction.class);
        String json = serializer.serialize(tx);

        assertTrue(json.contains("\"description\":\"Lunch\""));
        assertTrue(json.contains("\"notes\":\"Paid in cash\""));
        assertTrue(json.contains("\"amount\":50"));
        assertTrue(json.contains("\"category\""));
        assertTrue(json.contains("\"Food\""));
        assertTrue(json.contains("\"date\":\"2024-01-01\""));
    }

    /**
     * Tests serialization of a primitive integer.
     * 
     * Ensures that primitive values are serialized without quotes and
     * without additional formatting.
     */
    @Test
    @DisplayName("Serialize primitive int")
    void testSerializePrimitiveInt() throws DataSerializerException {
        DataSerializer<Integer> serializer = new JsonDataSerializer<>(Integer.class);
        assertEquals("42", serializer.serialize(42));
    }

    /**
     * Tests serialization of a string containing quotes.
     * 
     * Verifies that the serializer correctly escapes special characters
     * according to JSON rules.
     */
    @Test
    @DisplayName("Serialize String with escaping")
    void testSerializeString() throws DataSerializerException {
        DataSerializer<String> serializer = new JsonDataSerializer<>(String.class);
        assertEquals("\"Hello \\\"World\\\"\"", serializer.serialize("Hello \"World\""));
    }

    /**
     * Tests serialization of a {@code null} reference.
     * 
     * Ensures that null values are serialized as the JSON literal
     * {@code null}
     */
    @Test
    @DisplayName("Serialize null")
    void testSerializeNull() throws DataSerializerException {
        DataSerializer<Object> serializer = new JsonDataSerializer<>(Object.class);
        assertEquals("null", serializer.serialize(null));
    }
}
