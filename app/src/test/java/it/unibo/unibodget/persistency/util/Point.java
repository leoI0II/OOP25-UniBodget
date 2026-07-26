package it.unibo.unibodget.persistency.util;

/**
 * Immutable point in a 2D coordinate system.
 * 
 * <p>
 * This record is used in tests to verify serialization and deserialization
 * of simple structured data. It contains two integer components representing
 * the horizontal (x) and vertical (y) coordinates.
 * </p>
 *
 * @param x the horizontal coordinate
 * @param y the vertical coordinate
 */
public record Point(int x, int y) {

}
