package it.unibo.unibodget.model.dashboard.impl;

import it.unibo.unibodget.model.dashboard.api.CategoryService;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of {@link CategoryService}.
 * This implementation stores a local category summary map that can
 * later be replaced or populated from the general domain model.
 */
public final class DefaultCategoryService implements CategoryService {

    private final Map<String, Double> categorySummaries;

    /**
     * Creates an empty category service.
     */
    public DefaultCategoryService() {
        this(Map.of());
    }

    /**
     * Creates a category service initialized with the provided summaries.
     *
     * @param categorySummaries the initial category summary values
     */
    public DefaultCategoryService(final Map<String, Double> categorySummaries) {
        this.categorySummaries = new LinkedHashMap<>(categorySummaries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getCategorySummaries() {
        return Collections.unmodifiableMap(categorySummaries);
    }

    /**
     * Adds or replaces a category summary value.
     *
     * @param categoryName the category name
     * @param amount the aggregated amount associated with the category
     */
    public void putCategorySummary(final String categoryName, final double amount) {
        categorySummaries.put(categoryName, amount);
    }
}