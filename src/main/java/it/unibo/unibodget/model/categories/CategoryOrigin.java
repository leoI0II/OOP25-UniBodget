package it.unibo.unibodget.model.categories;

/**
 * Represents the origin of a category.
 *
 * <p>DEFAULT categories are built into the application and cannot be archived. CUSTOM categories
 * are created by the user and can be archived/reactivated.
 */
public enum CategoryOrigin {
    /** Built-in category provided by the application. */
    DEFAULT,
    /** User-defined custom category. */
    CUSTOM
}
