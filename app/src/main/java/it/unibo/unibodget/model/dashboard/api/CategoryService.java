package it.unibo.unibodget.model.dashboard.api;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Exposes category-related information required by the dashboard.
 * This service acts as the category-oriented entry point for summary
 * data used by charts, filters, and budget breakdowns.
 */
public interface CategoryService {

    /**
     * Returns aggregated values grouped by category name.
     *
     * @return a map whose keys are category names and whose values
     *         are the corresponding aggregated amounts
     */
    Map<String, BigDecimal> getCategorySummaries();
}