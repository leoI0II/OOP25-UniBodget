
package it.unibo.unibodget.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import it.unibo.unibodget.model.utils.ARGBColor;

import static org.junit.jupiter.api.Assertions.*;

class ARGBColorTest {

    // 1. Test valid ARGB values (0-255) and ensure they are stored correctly
    @Test
    void testValidComponents() {
        ARGBColor black = new ARGBColor(255, 0, 0, 0);
        assertEquals(255, black.alpha());
        assertEquals(0, black.red());
        
        ARGBColor white = new ARGBColor(255, 255, 255, 255);
        assertEquals(255, white.blue());
    }

    // 2. Test constructor with RGB values and ensure alpha defaults to 255
    @Test
    void testRgbConstructorAddsFullAlpha() {
        ARGBColor color = new ARGBColor(100, 150, 200);
        assertEquals(255, color.alpha(), "Alpha should be 255 by default for RGB constructor");
        assertEquals(100, color.red());
    }

    // 3. Test bit shifting constructor to ensure correct parsing of ARGB integer
    @Test
    void testIntConstructor() {
        // 0xAA (170), 0xBB (187), 0xCC (204), 0xDD (221)
        ARGBColor color = new ARGBColor(0xAABBCCDD);
        assertEquals(170, color.alpha());
        assertEquals(187, color.red());
        assertEquals(204, color.green());
        assertEquals(221, color.blue());
    }

    // 4. Test hex string constructor with various valid formats and ensure correct parsing
    @ParameterizedTest
    @ValueSource(strings = {"#FF0000", "FF0000", "#FFFF0000", "FFFF0000"})
    void testValidHexParsing(String hex) {
        ARGBColor color = new ARGBColor(hex);
        assertEquals(255, color.alpha());
        assertEquals(255, color.red());
        assertEquals(0, color.green());
        assertEquals(0, color.blue());
    }

    // 5. Test hex string output to ensure it matches the expected format and values
    @Test
    void testToHexString() {
        ARGBColor color = new ARGBColor(10, 0, 255, 15);
        // 10 = 0A, 0 = 00, 255 = FF, 15 = 0F
        assertEquals("#0A00FF0F", color.toHexString());
        
        ARGBColor red = new ARGBColor(255, 255, 0, 0);
        assertEquals("#FFFF0000", red.toHexString());
    }

    // 6. check if the class fails when components are out of bounds (less than 0 or greater than 255) 
    // and throws the correct exception with a clear message
    @ParameterizedTest
    @CsvSource({
        "-1, 0, 0, 0",
        "256, 0, 0, 0",
        "255, -10, 0, 0",
        "255, 255, 256, 0"
    })
    void testComponentOutOfBounds(int a, int r, int g, int b) {
        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> new ARGBColor(a, r, g, b)
        );
        assertTrue(thrown.getMessage().contains("must be between 0 and 255"));
    }

    // 7. check if the hex string constructor fails when the string is not in the correct format (not 6 or 8 characters after removing #)
    @ParameterizedTest
    @ValueSource(strings = {"#12345", "1234567", "#FF", "", "#123456789"})
    void testInvalidHexLength(String invalidHex) {
        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> new ARGBColor(invalidHex)
        );
        assertTrue(thrown.getMessage().contains("must be in the format #RRGGBB or #AARRGGBB"));
    }

    // 8. check if the hex string constructor fails when the string contains invalid hexadecimal characters and throws the correct exception
    @Test
    void testInvalidHexCharacters() {
        assertThrows(
            NumberFormatException.class,
            () -> new ARGBColor("#ZZZZZZ")
        );
    }

    // ==========================================
    // TESTS for auto-generated methods(equals, hashCode, toString)
    // ==========================================

    // 9. Test equals auto-generated method
    @Test
    void testEquals() {
        ARGBColor color1 = new ARGBColor(255, 100, 150, 200);
        ARGBColor color2 = new ARGBColor(255, 100, 150, 200);
        ARGBColor colorDifferent = new ARGBColor(128, 100, 150, 200);

        assertEquals(color1, color1);
        assertEquals(color1, color2);
        assertEquals(color2, color1);
        assertNotEquals(color1, colorDifferent);
        assertNotEquals(color1, null);
        assertNotEquals(color1, "Just a string");
    }

    // 10. Test hashCode auto-generated method
    @Test
    void testHashCode() {
        ARGBColor color1 = new ARGBColor(255, 100, 150, 200);
        ARGBColor color2 = new ARGBColor(255, 100, 150, 200);
        ARGBColor colorDifferent = new ARGBColor(128, 100, 150, 200);

        assertEquals(color1.hashCode(), color2.hashCode());
        assertNotEquals(color1.hashCode(), colorDifferent.hashCode());
    }

    // 11. Test toString auto-generated method
    @Test
    void testToString() {
        ARGBColor color = new ARGBColor(128, 10, 20, 30);
        String str = color.toString();

        assertTrue(str.startsWith("ARGBColor["));
        assertTrue(str.contains("alpha=128"));
        assertTrue(str.contains("red=10"));
        assertTrue(str.contains("green=20"));
        assertTrue(str.contains("blue=30"));
        assertTrue(str.endsWith("]"));
    }
}