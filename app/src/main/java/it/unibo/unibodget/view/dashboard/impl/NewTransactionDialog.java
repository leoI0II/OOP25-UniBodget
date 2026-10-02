package it.unibo.unibodget.view.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryCatalog;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.dashboard.impl.FriendLoanSummary;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

/**
 * Dialog used to create or edit a {@link CashTransaction}.
 *
 * <p>
 * The dialog always returns a {@link NewTransactionRequest}, leaving the final
 * construction of the transaction entity to the controller.
 * </p>
 *
 * <p>
 * The dialog supports two modes:
 * </p>
 * <ul>
 * <li>creation mode, when no existing transaction is provided,</li>
 * <li>edit mode, when an existing transaction is provided and all editable
 * fields are pre-filled.</li>
 * </ul>
 */
public final class NewTransactionDialog extends Dialog<NewTransactionRequest> {

    private final TextField descriptionField;
    private final TextField amountField;
    private final DatePicker datePicker;
    private final ComboBox<CategoryType> transactionTypeBox;
    private final ComboBox<Category> categoryBox;
    private final TextArea notesArea;
    private final TextField friendNameField;
    private final ComboBox<FriendLoanOperation> friendLoanOperationBox;
    private final ComboBox<FriendLoanSummary> existingFriendLoanBox;
    private final Label walletCurrencyLabel;

    private final CategoryCatalog categoryCatalog;
    private final List<FriendLoanSummary> openFriendLoans;
    private final BiFunction<NewCategoryRequest, CategoryType, Optional<Category>> categoryCreator;
    private final boolean editMode;

    /**
     * Creates a dialog in creation mode.
     *
     * @param walletCurrency
     *            the currency of the current wallet; must not be {@code null}
     * @param categoryCatalog
     *            the available category catalog; must not be {@code null}
     * @param openFriendLoans
     *            the currently open friend loans; must not be {@code null}
     * @param categoryCreator
     *            callback used to create a new custom category; must not be
     *            {@code null}
     */
    public NewTransactionDialog(
            final CurrencyUnit walletCurrency,
            final CategoryCatalog categoryCatalog,
            final List<FriendLoanSummary> openFriendLoans,
            final BiFunction<NewCategoryRequest, CategoryType, Optional<Category>> categoryCreator) {
        this(walletCurrency, categoryCatalog, openFriendLoans, categoryCreator, null);
    }

    /**
     * Creates a dialog in creation or edit mode.
     *
     * @param walletCurrency
     *            the currency of the current wallet; must not be {@code null}
     * @param categoryCatalog
     *            the available category catalog; must not be {@code null}
     * @param openFriendLoans
     *            the currently open friend loans; must not be {@code null}
     * @param categoryCreator
     *            callback used to create a new custom category; must not be
     *            {@code null}
     * @param transactionToEdit
     *            the transaction to edit, or {@code null} to create a new one
     */
    public NewTransactionDialog(
            final CurrencyUnit walletCurrency,
            final CategoryCatalog categoryCatalog,
            final List<FriendLoanSummary> openFriendLoans,
            final BiFunction<NewCategoryRequest, CategoryType, Optional<Category>> categoryCreator,
            final CashTransaction transactionToEdit) {

        this.categoryCatalog = Objects.requireNonNull(categoryCatalog);
        this.openFriendLoans = List.copyOf(Objects.requireNonNull(openFriendLoans));
        this.categoryCreator = Objects.requireNonNull(categoryCreator);
        this.editMode = transactionToEdit != null;

        setTitle(editMode ? "Edit Transaction" : "Create Transaction");
        setHeaderText(editMode ? "Update transaction details" : "Insert transaction details");

        final ButtonType confirmButtonType =
                new ButtonType(editMode ? "Save" : "Create", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        this.descriptionField = new TextField();
        this.amountField = new TextField();
        this.datePicker = new DatePicker(LocalDate.now());
        this.transactionTypeBox = new ComboBox<>();
        this.categoryBox = new ComboBox<>();
        this.notesArea = new TextArea();
        this.friendNameField = new TextField();
        this.friendLoanOperationBox = new ComboBox<>();
        this.existingFriendLoanBox = new ComboBox<>();
        this.walletCurrencyLabel = new Label(String.valueOf(Objects.requireNonNull(walletCurrency)));

        descriptionField.setPromptText("Description");
        amountField.setPromptText("Amount");
        notesArea.setPromptText("Optional notes");
        notesArea.setPrefRowCount(3);
        friendNameField.setPromptText("Friend name");

        configureTransactionTypeBox();
        configureCategoryBox();
        configureFriendLoanBoxes();

        final Button newCategoryButton = new Button("New category");
        newCategoryButton.setOnAction(event -> openNewCategoryDialog());

        transactionTypeBox.getSelectionModel().select(CategoryType.EXPENSE);
        updateCategoriesFor(transactionTypeBox.getValue());

        transactionTypeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateCategoriesFor(newValue);
            updateFriendLoanControls();
        });
        friendLoanOperationBox.valueProperty().addListener((obs, oldValue, newValue) -> updateFriendLoanControls());

        final GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        final HBox categoryRow = new HBox(8, categoryBox, newCategoryButton);

        grid.add(new Label("Transaction type:"), 0, 0);
        grid.add(transactionTypeBox, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryRow, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Amount:"), 0, 3);
        grid.add(amountField, 1, 3);
        grid.add(new Label("Wallet currency:"), 0, 4);
        grid.add(walletCurrencyLabel, 1, 4);
        grid.add(new Label("Date:"), 0, 5);
        grid.add(datePicker, 1, 5);
        grid.add(new Label("Friend loan mode:"), 0, 6);
        grid.add(friendLoanOperationBox, 1, 6);
        grid.add(new Label("Friend name:"), 0, 7);
        grid.add(friendNameField, 1, 7);
        grid.add(new Label("Existing loan:"), 0, 8);
        grid.add(existingFriendLoanBox, 1, 8);
        grid.add(new Label("Notes:"), 0, 9);
        grid.add(notesArea, 1, 9);

        getDialogPane().setContent(grid);

        if (editMode) {
            prefillFromTransaction(transactionToEdit);
        } else {
            updateFriendLoanControls();
        }

        final Node confirmButton = getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);

        final Runnable validator = () -> confirmButton.setDisable(!isFormValid());

        descriptionField.textProperty().addListener((obs, oldValue, newValue) -> validator.run());
        amountField.textProperty().addListener((obs, oldValue, newValue) -> validator.run());
        datePicker.valueProperty().addListener((obs, oldValue, newValue) -> validator.run());
        transactionTypeBox.valueProperty().addListener((obs, oldValue, newValue) -> validator.run());
        categoryBox.valueProperty().addListener((obs, oldValue, newValue) -> validator.run());
        friendLoanOperationBox.valueProperty().addListener((obs, oldValue, newValue) -> validator.run());
        friendNameField.textProperty().addListener((obs, oldValue, newValue) -> validator.run());
        existingFriendLoanBox.valueProperty().addListener((obs, oldValue, newValue) -> validator.run());

        validator.run();

        setResultConverter(buttonType -> {
            if (!Objects.equals(buttonType, confirmButtonType)) {
                return null;
            }
            return buildRequest().orElse(null);
        });
    }

    /**
     * Configures the transaction type combo box.
     */
    private void configureTransactionTypeBox() {
        transactionTypeBox.setItems(FXCollections.observableArrayList(
                CategoryType.INCOME,
                CategoryType.EXPENSE,
                CategoryType.TRANSFER,
                CategoryType.FRIEND_LOAN
        ));
        transactionTypeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final CategoryType type) {
                return type == null ? "" : formatCategoryType(type);
            }

            @Override
            public CategoryType fromString(final String string) {
                return null;
            }
        });
        transactionTypeBox.setCellFactory(listView -> createTextCell(transactionTypeBox.getConverter()::toString));
        transactionTypeBox.setButtonCell(createTextCell(transactionTypeBox.getConverter()::toString));
        transactionTypeBox.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Configures the category combo box.
     */
    private void configureCategoryBox() {
        categoryBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final Category category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public Category fromString(final String string) {
                return null;
            }
        });
        categoryBox.setCellFactory(listView -> createTextCell(categoryBox.getConverter()::toString));
        categoryBox.setButtonCell(createTextCell(categoryBox.getConverter()::toString));
        categoryBox.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Configures the friend-loan-specific combo boxes.
     */
    private void configureFriendLoanBoxes() {
        friendLoanOperationBox.setItems(FXCollections.observableArrayList(
                FriendLoanOperation.NEW_LOAN,
                FriendLoanOperation.REPAYMENT
        ));
        friendLoanOperationBox.getSelectionModel().select(FriendLoanOperation.NEW_LOAN);
        friendLoanOperationBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final FriendLoanOperation operation) {
                if (operation == null) {
                    return "";
                }
                return switch (operation) {
                    case NEW_LOAN -> "New loan";
                    case REPAYMENT -> "Repayment";
                };
            }

            @Override
            public FriendLoanOperation fromString(final String string) {
                return null;
            }
        });
        friendLoanOperationBox.setCellFactory(listView -> createTextCell(friendLoanOperationBox.getConverter()::toString));
        friendLoanOperationBox.setButtonCell(createTextCell(friendLoanOperationBox.getConverter()::toString));
        friendLoanOperationBox.setMaxWidth(Double.MAX_VALUE);

        existingFriendLoanBox.setItems(FXCollections.observableArrayList(openFriendLoans));
        existingFriendLoanBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final FriendLoanSummary summary) {
                return summary == null
                        ? ""
                        : summary.getFriendName()
                                + " - residual "
                                + summary.getNetBalance().abs().stripTrailingZeros().toPlainString();
            }

            @Override
            public FriendLoanSummary fromString(final String string) {
                return null;
            }
        });
        existingFriendLoanBox.setCellFactory(listView -> createTextCell(existingFriendLoanBox.getConverter()::toString));
        existingFriendLoanBox.setButtonCell(createTextCell(existingFriendLoanBox.getConverter()::toString));
        existingFriendLoanBox.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Prefills the dialog from an existing transaction.
     *
     * @param transaction
     *            the transaction being edited
     */
    private void prefillFromTransaction(final CashTransaction transaction) {
        final CategoryType type = transaction.getCategory().getType();

        descriptionField.setText(transaction.getDescription());
        amountField.setText(transaction.getAsset().amount().abs().stripTrailingZeros().toPlainString());
        datePicker.setValue(transaction.getDate());
        notesArea.setText(transaction.getNotes() == null ? "" : transaction.getNotes());

        transactionTypeBox.getSelectionModel().select(type);
        updateCategoriesFor(type);

        categoryBox.getSelectionModel().select(
                categoryBox.getItems().stream()
                        .filter(category -> category.getName().equalsIgnoreCase(transaction.getCategory().getName()))
                        .findFirst()
                        .orElse(null)
        );

        if (type == CategoryType.FRIEND_LOAN && transaction.isFriendLoanTransaction()) {
            final boolean repayment = transaction.getAsset().amount().signum() > 0;
            friendLoanOperationBox.getSelectionModel().select(
                    repayment ? FriendLoanOperation.REPAYMENT : FriendLoanOperation.NEW_LOAN
            );
            updateFriendLoanControls();

            if (repayment) {
                final UUID loanId = transaction.getFriendLoanId().orElse(null);
                existingFriendLoanBox.getSelectionModel().select(
                        openFriendLoans.stream()
                                .filter(summary -> summary.getFriendLoanId().equals(loanId))
                                .findFirst()
                                .orElse(null)
                );
            } else {
                friendNameField.setText(transaction.getFriendName().orElse(""));
            }
        } else {
            updateFriendLoanControls();
        }
    }

    /**
     * Updates friend-loan controls according to the selected transaction type
     * and selected friend-loan operation.
     */
    private void updateFriendLoanControls() {
        final boolean friendLoan = transactionTypeBox.getValue() == CategoryType.FRIEND_LOAN;
        final FriendLoanOperation operation = friendLoanOperationBox.getValue();

        friendLoanOperationBox.setDisable(!friendLoan);

        final boolean newLoan = friendLoan && operation == FriendLoanOperation.NEW_LOAN;
        final boolean repayment = friendLoan && operation == FriendLoanOperation.REPAYMENT;

        friendNameField.setDisable(!newLoan);
        existingFriendLoanBox.setDisable(!repayment);

        if (!newLoan) {
            friendNameField.clear();
        }
        if (!repayment) {
            existingFriendLoanBox.getSelectionModel().clearSelection();
        }
    }

    /**
     * Returns whether the current form content is valid.
     *
     * @return {@code true} if the form is valid
     */
    private boolean isFormValid() {
        final boolean hasDescription = descriptionField.getText() != null
                && !descriptionField.getText().trim().isEmpty();
        final BigDecimal amount = parseAmount(amountField.getText());
        final boolean validAmount = amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
        final boolean hasDate = datePicker.getValue() != null;
        final boolean hasType = transactionTypeBox.getValue() != null;
        final boolean hasCategory = categoryBox.getValue() != null;

        if (!(hasDescription && validAmount && hasDate && hasType && hasCategory)) {
            return false;
        }

        if (transactionTypeBox.getValue() != CategoryType.FRIEND_LOAN) {
            return true;
        }

        final FriendLoanOperation operation = friendLoanOperationBox.getValue();
        if (operation == null) {
            return false;
        }

        return switch (operation) {
            case NEW_LOAN -> friendNameField.getText() != null && !friendNameField.getText().trim().isEmpty();
            case REPAYMENT -> existingFriendLoanBox.getValue() != null;
        };
    }

    /**
     * Builds the dialog result request if the current content is valid.
     *
     * @return the request, if valid
     */
    private Optional<NewTransactionRequest> buildRequest() {
        if (!isFormValid()) {
            return Optional.empty();
        }

        final BigDecimal amount = parseAmount(amountField.getText());
        final CategoryType type = transactionTypeBox.getValue();
        final Category category = categoryBox.getValue();
        final LocalDate date = datePicker.getValue();
        final String description = descriptionField.getText().trim();
        final String notes = notesArea.getText() == null || notesArea.getText().isBlank()
                ? null
                : notesArea.getText().trim();

        String friendName = null;
        FriendLoanOperation operation = null;
        UUID existingFriendLoanId = null;

        if (type == CategoryType.FRIEND_LOAN) {
            operation = friendLoanOperationBox.getValue();
            if (operation == FriendLoanOperation.NEW_LOAN) {
                friendName = friendNameField.getText().trim();
            } else {
                final FriendLoanSummary selectedLoan = existingFriendLoanBox.getValue();
                existingFriendLoanId = selectedLoan.getFriendLoanId();
                friendName = selectedLoan.getFriendName();
            }
        }

        return Optional.of(new NewTransactionRequest(
                amount,
                type,
                category,
                date,
                description,
                notes,
                friendName,
                operation,
                existingFriendLoanId
        ));
    }

    /**
     * Opens the custom-category creation dialog.
     */
    private void openNewCategoryDialog() {
        final CategoryType selectedType = transactionTypeBox.getValue();
        if (selectedType == null) {
            return;
        }

        final NewCategoryDialog dialog = new NewCategoryDialog(selectedType);
        dialog.showAndWait().ifPresent(request ->
                categoryCreator.apply(request, selectedType).ifPresent(createdCategory -> {
                    updateCategoriesFor(selectedType);
                    categoryBox.getSelectionModel().select(createdCategory);
                })
        );
    }

    /**
     * Refreshes the category combo box according to the selected transaction
     * type.
     *
     * @param selectedType
     *            the selected transaction type
     */
    private void updateCategoriesFor(final CategoryType selectedType) {
        final List<Category> filteredCategories = categoryCatalog.getActiveCategories().stream()
                .filter(category -> category.getType() == selectedType)
                .toList();

        final Category previousSelection = categoryBox.getValue();
        categoryBox.setItems(FXCollections.observableArrayList(filteredCategories));

        if (previousSelection != null) {
            filteredCategories.stream()
                    .filter(category -> category.getName().equalsIgnoreCase(previousSelection.getName()))
                    .findFirst()
                    .ifPresentOrElse(
                            category -> categoryBox.getSelectionModel().select(category),
                            () -> {
                                if (!filteredCategories.isEmpty()) {
                                    categoryBox.getSelectionModel().selectFirst();
                                } else {
                                    categoryBox.getSelectionModel().clearSelection();
                                }
                            }
                    );
        } else if (!filteredCategories.isEmpty()) {
            categoryBox.getSelectionModel().selectFirst();
        } else {
            categoryBox.getSelectionModel().clearSelection();
        }
    }

    /**
     * Parses a decimal amount from the given text.
     *
     * @param text
     *            the raw text
     * @return the parsed amount, or {@code null} if parsing fails
     */
    private static BigDecimal parseAmount(final String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            return new BigDecimal(text.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Formats a category type for UI display.
     *
     * @param type
     *            the category type
     * @return the user-facing label
     */
    private static String formatCategoryType(final CategoryType type) {
        return switch (type) {
            case INCOME -> "Income";
            case EXPENSE -> "Expense";
            case TRANSFER -> "Transfer";
            case FRIEND_LOAN -> "Friend loan";
        };
    }

    /**
     * Creates a text-only list cell using the given text mapper.
     *
     * @param mapper
     *            the text mapper
     * @param <T>
     *            the cell item type
     * @return a configured list cell
     */
    private static <T> ListCell<T> createTextCell(final java.util.function.Function<T, String> mapper) {
        return new ListCell<>() {
            @Override
            protected void updateItem(final T item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : mapper.apply(item));
            }
        };
    }
}