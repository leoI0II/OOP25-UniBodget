package it.unibo.unibodget.model.dashboard.specification;

import java.time.LocalDate;
import java.util.Objects;

import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.dashboard.impl.TransactionFilterCriteria;

/**
 * Factory and utility class for creating {@link TransactionSpecification} instances.
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    /**
     * Creates a specification that matches all transactions.
     *
     * @return an unconditional specification
     */
    public static TransactionSpecification all() {
        return TransactionSpecification.all();
    }

    /**
     * Creates a category name specification.
     *
     * @param categoryName the category name to match
     * @return a category name specification
     */
    public static TransactionSpecification categoryName(final String categoryName) {
        return new CategoryNameSpecification(categoryName);
    }

    /**
     * Creates a category type specification.
     *
     * @param categoryType the category type to match
     * @return a category type specification
     */
    public static TransactionSpecification categoryType(final CategoryType categoryType) {
        return new CategoryTypeSpecification(categoryType);
    }

    /**
     * Creates a date range specification.
     *
     * @param fromDate the inclusive start date
     * @param toDate the inclusive end date
     * @return a date range specification
     */
    public static TransactionSpecification dateBetween(final LocalDate fromDate, final LocalDate toDate) {
        return new DateRangeSpecification(fromDate, toDate);
    }

    /**
     * Creates a keyword specification.
     *
     * @param keyword the keyword to search for
     * @return a keyword specification
     */
    public static TransactionSpecification keyword(final String keyword) {
        return new KeywordSpecification(keyword);
    }

    /**
     * Translates a {@link TransactionFilterCriteria} object into a composite {@link TransactionSpecification}.
     *
     * @param criteria the filter criteria to translate
     * @return a combined specification representing all active criteria
     */
    public static TransactionSpecification fromCriteria(final TransactionFilterCriteria criteria) {
        Objects.requireNonNull(criteria, "criteria must not be null");

        TransactionSpecification spec = TransactionSpecification.all();

        if (criteria.getCategoryName().isPresent()) {
            spec = spec.and(categoryName(criteria.getCategoryName().get()));
        }

        if (criteria.getCategoryType().isPresent()) {
            spec = spec.and(categoryType(criteria.getCategoryType().get()));
        }

        if (criteria.getFromDate().isPresent() || criteria.getToDate().isPresent()) {
            spec = spec.and(dateBetween(
                    criteria.getFromDate().orElse(null),
                    criteria.getToDate().orElse(null)
            ));
        }

        if (criteria.getKeyword().isPresent()) {
            spec = spec.and(keyword(criteria.getKeyword().get()));
        }

        return spec;
    }
}
