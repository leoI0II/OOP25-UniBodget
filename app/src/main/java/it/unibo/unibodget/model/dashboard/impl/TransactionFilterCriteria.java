package it.unibo.unibodget.model.dashboard.impl;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import it.unibo.unibodget.model.categories.CategoryType;

/**
 * Immutable criteria object used to filter and sort transaction history.
 *
 * <p>All filter fields are optional. Missing values mean that the corresponding
 * filtering condition is not applied. Sorting always has a value and defaults
 * to {@link TransactionSortOrder#NEWEST_FIRST}.</p>
 */
public final class TransactionFilterCriteria {

    private final String categoryName;
    private final CategoryType categoryType;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final String keyword;
    private final TransactionSortOrder sortOrder;

    /**
     * Creates empty filter criteria with default newest-first sorting.
     */
    public TransactionFilterCriteria() {
        this(null, null, null, null, null, TransactionSortOrder.NEWEST_FIRST);
    }

    /**
     * Creates filter criteria with the given optional values.
     *
     * @param categoryName
     *            the category name to match, or {@code null} for no category filter
     * @param categoryType
     *            the category type to match, or {@code null} for no type filter
     * @param fromDate
     *            the start date inclusive, or {@code null} for no lower bound
     * @param toDate
     *            the end date inclusive, or {@code null} for no upper bound
     * @param keyword
     *            a keyword to search in description or notes, or {@code null}
     * @param sortOrder
     *            the sort order to apply
     */
    public TransactionFilterCriteria(
            final String categoryName,
            final CategoryType categoryType,
            final LocalDate fromDate,
            final LocalDate toDate,
            final String keyword,
            final TransactionSortOrder sortOrder) {
        this.categoryName = normalize(categoryName);
        this.categoryType = categoryType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.keyword = normalize(keyword);
        this.sortOrder = Objects.requireNonNull(sortOrder);
    }

    /**
     * Returns the category name filter, if present.
     *
     * @return the category name filter
     */
    public Optional<String> getCategoryName() {
        return Optional.ofNullable(categoryName);
    }

    /**
     * Returns the category type filter, if present.
     *
     * @return the category type filter
     */
    public Optional<CategoryType> getCategoryType() {
        return Optional.ofNullable(categoryType);
    }

    /**
     * Returns the start date filter, if present.
     *
     * @return the start date filter
     */
    public Optional<LocalDate> getFromDate() {
        return Optional.ofNullable(fromDate);
    }

    /**
     * Returns the end date filter, if present.
     *
     * @return the end date filter
     */
    public Optional<LocalDate> getToDate() {
        return Optional.ofNullable(toDate);
    }

    /**
     * Returns the keyword filter, if present.
     *
     * @return the keyword filter
     */
    public Optional<String> getKeyword() {
        return Optional.ofNullable(keyword);
    }

    /**
     * Returns the sort order to apply to the filtered result.
     *
     * @return the requested sort order
     */
    public TransactionSortOrder getSortOrder() {
        return sortOrder;
    }

    private static String normalize(final String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}