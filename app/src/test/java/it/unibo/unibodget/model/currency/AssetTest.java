package it.unibo.unibodget.model.currency;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class AssetTest {

    @Test
    void shouldCreateAsset() {
        Asset a = new Asset(FiatCurrency.EUR, BigDecimal.TEN);

        assertEquals(FiatCurrency.EUR, a.currency());
        assertEquals(BigDecimal.TEN, a.amount());
    }

    @Test
    void shouldAddAssets() {
        Asset a1 = new Asset(FiatCurrency.EUR, BigDecimal.TEN);
        Asset a2 = new Asset(FiatCurrency.EUR, BigDecimal.ONE);

        Asset result = a1.add(a2);

        assertEquals(new BigDecimal("11"), result.amount());
    }

    @Test
    void shouldSubtractAssets() {
        Asset a1 = new Asset(FiatCurrency.EUR, BigDecimal.TEN);
        Asset a2 = new Asset(FiatCurrency.EUR, BigDecimal.ONE);

        Asset result = a1.subtract(a2);

        assertEquals(new BigDecimal("9"), result.amount());
    }

    @Test
    void shouldNegateAsset() {
        Asset a = new Asset(FiatCurrency.EUR, BigDecimal.TEN);

        assertEquals(new BigDecimal("-10"), a.negate().amount());
    }

    @Test
    void shouldMultiplyAssets() {
        Asset a1 = new Asset(FiatCurrency.EUR, new BigDecimal("2"));
        Asset a2 = new Asset(FiatCurrency.EUR, new BigDecimal("3"));

        assertEquals(new BigDecimal("6"), a1.multiply(a2).amount());
    }

    @Test
    void shouldMultiplyByScalar() {
        Asset a = new Asset(FiatCurrency.EUR, new BigDecimal("2"));

        assertEquals(new BigDecimal("6"), a.multiply(new BigDecimal("3")).amount());
    }

    @Test
    void shouldDivideAssets() {
        Asset a1 = new Asset(FiatCurrency.EUR, new BigDecimal("10"));
        Asset a2 = new Asset(FiatCurrency.EUR, new BigDecimal("2"));

        assertEquals(new BigDecimal("5"), a1.divide(a2).amount());
    }

    @Test
    void shouldDetectPositiveNegativeZero() {
        assertTrue(new Asset(FiatCurrency.EUR, new BigDecimal("5")).isPositive());
        assertTrue(new Asset(FiatCurrency.EUR, new BigDecimal("-5")).isNegative());
        assertTrue(new Asset(FiatCurrency.EUR, BigDecimal.ZERO).isZero());
    }

    @Test
    void shouldCompareAssets() {
        Asset a1 = new Asset(FiatCurrency.EUR, new BigDecimal("5"));
        Asset a2 = new Asset(FiatCurrency.EUR, new BigDecimal("10"));

        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
        assertEquals(0, a1.compareTo(new Asset(FiatCurrency.EUR, new BigDecimal("5"))));
    }

    @Test
    void shouldFailOnDifferentCurrencies() {
        Asset a1 = new Asset(FiatCurrency.EUR, BigDecimal.ONE);
        Asset a2 = new Asset(FiatCurrency.USD, BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> a1.add(a2));
        assertThrows(IllegalArgumentException.class, () -> a1.subtract(a2));
        assertThrows(IllegalArgumentException.class, () -> a1.multiply(a2));
        assertThrows(IllegalArgumentException.class, () -> a1.divide(a2));
        assertThrows(IllegalArgumentException.class, () -> a1.compareTo(a2));
    }

}
