package it.unibo.unibodget.view.dashboard.impl;

/**
 * Immutable dialog result representing the raw user input required to create a
 * new custom category.
 *
 * @param name
 *            the category name entered by the user
 * @param colorHex
 *            the hexadecimal RGB color entered by the user
 */
public record NewCategoryRequest(
        String name,
        String colorHex) {
}