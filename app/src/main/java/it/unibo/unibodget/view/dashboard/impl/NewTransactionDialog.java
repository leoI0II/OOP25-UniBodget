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
 * Dialog used to collect the raw user input required to create a new cash
 * transaction.
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

    public NewTransactionDialog(
            final CurrencyUnit walletCurrency,
            final CategoryCatalog categoryCatalog,
            final List<FriendLoanSummary> openFriendLoans,
            final BiFunction<NewCategoryRequest, CategoryType, Optional<Category>> categoryCreator) {
        setTitle("Create New Transaction");
        setHeaderText("Insert transaction details");

        final ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

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
        this.categoryCatalog = Objects.requireNonNull(categoryCatalog);
        this.openFriendLoans = List.copyOf(Objects.requireNonNull(openFriendLoans));
        this.categoryCreator = Objects.requireNonNull(categoryCreator);

        descriptionField.setPromptText("Description");
        amountField.setPromptText("Amount");
        notesArea.setPromptText("Optional notes");
        notesArea.setPrefRowCount(3);
        friendNameField.setPromptText("Friend name");

        transactionTypeBox.setItems(FXCollections.observableArrayList(
                CategoryType.INCOME,
                CategoryType.EXPENSE,
                CategoryType.TRANSFER,
                CategoryType.FRIEND_LOAN
        ));
        transactionTypeBox.setMaxWidth(Double.MAX_VALUE);
        transactionTypeBox.getSelectionModel().select(CategoryType.EXPENSE);

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

        transactionTypeBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(final CategoryType item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCategoryType(item));
            }
        });

        transactionTypeBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final CategoryType item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCategoryType(item));
            }
        });

        categoryBox.setMaxWidth(Double.MAX_VALUE);
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

        categoryBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(final Category item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        categoryBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final Category item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        friendLoanOperationBox.setItems(FXCollections.observableArrayList(
                FriendLoanOperation.NEW_LOAN,
                FriendLoanOperation.REPAYMENT
        ));
        friendLoanOperationBox.setMaxWidth(Double.MAX_VALUE);
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

        friendLoanOperationBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(final FriendLoanOperation item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(friendLoanOperationBox.getConverter().toString(item));
            }
        });

        friendLoanOperationBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final FriendLoanOperation item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(friendLoanOperationBox.getConverter().toString(item));
            }
        });

        existingFriendLoanBox.setItems(FXCollections.observableArrayList(this.openFriendLoans));
        existingFriendLoanBox.setMaxWidth(Double.MAX_VALUE);
        existingFriendLoanBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final FriendLoanSummary summary) {
                return summary == null
                        ? ""
                        : summary.getFriendName() + " - residual " + summary.getNetBalance().abs().stripTrailingZeros().toPlainString();
            }

            @Override
            public FriendLoanSummary fromString(final String string) {
                return null;
            }
        });

        existingFriendLoanBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(final FriendLoanSummary item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : existingFriendLoanBox.getConverter().toString(item));
            }
        });

        existingFriendLoanBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final FriendLoanSummary item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : existingFriendLoanBox.getConverter().toString(item));
            }
        });

        final Button newCategoryButton = new Button("New category");
        newCategoryButton.setOnAction(event -> openNewCategoryDialog());

        updateCategoriesFor(transactionTypeBox.getValue());

        transactionTypeBox.valueProperty().addListener((obs, oldType, newType) -> {
            updateCategoriesFor(newType);
            updateFriendLoanControls();
        });

        friendLoanOperationBox.valueProperty().addListener((obs, oldValue, newValue) -> updateFriendLoanControls());

        updateFriendLoanControls();

        final HBox categoryRow = new HBox(8, categoryBox, newCategoryButton);

        final GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

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

        final Node createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        final Runnable validate = () -> {
            final boolean descriptionOk = descriptionField.getText() != null
                    && !descriptionField.getText().trim().isEmpty();
            final BigDecimal parsedAmount = parseAmount(amountField.getText());
            final boolean amountOk = parsedAmount != null
                    && parsedAmount.compareTo(BigDecimal.ZERO) > 0;
            final boolean dateOk = datePicker.getValue() != null;
            final boolean typeOk = transactionTypeBox.getValue() != null;
            final boolean categoryOk = categoryBox.getValue() != null;

            boolean friendLoanOk = true;
            if (transactionTypeBox.getValue() == CategoryType.FRIEND_LOAN) {
                final FriendLoanOperation op = friendLoanOperationBox.getValue();
                friendLoanOk = op != null;
                if (op == FriendLoanOperation.NEW_LOAN) {
                    friendLoanOk = friendLoanOk
                            && friendNameField.getText() != null
                            && !friendNameField.getText().trim().isEmpty();
                } else if (op == FriendLoanOperation.REPAYMENT) {
                    friendLoanOk = friendLoanOk && existingFriendLoanBox.getValue() != null;
                }
            }

            createButton.setDisable(!(descriptionOk && amountOk && dateOk && typeOk && categoryOk && friendLoanOk));
        };

        descriptionField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
        amountField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
        datePicker.valueProperty().addListener((obs, oldValue, newValue) -> validate.run());
        transactionTypeBox.valueProperty().addListener((obs, oldValue, newValue) -> validate.run());
        categoryBox.valueProperty().addListener((obs, oldValue, newValue) -> validate.run());
        friendNameField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
        friendLoanOperationBox.valueProperty().addListener((obs, oldValue, newValue) -> validate.run());
        existingFriendLoanBox.valueProperty().addListener((obs, oldValue, newValue) -> validate.run());

        validate.run();

        setResultConverter(buttonType -> {
            if (!Objects.equals(buttonType, createButtonType)) {
                return null;
            }

            final BigDecimal amount = parseAmount(amountField.getText());
            final CategoryType transactionType = transactionTypeBox.getValue();
            final Category category = categoryBox.getValue();
            final LocalDate date = datePicker.getValue();
            final String description = descriptionField.getText().trim();
            final String notes = notesArea.getText() == null || notesArea.getText().isBlank()
                    ? null
                    : notesArea.getText().trim();

            String friendName = null;
            FriendLoanOperation friendLoanOperation = null;
            UUID existingFriendLoanId = null;

            if (transactionType == CategoryType.FRIEND_LOAN) {
                friendLoanOperation = friendLoanOperationBox.getValue();
                if (friendLoanOperation == FriendLoanOperation.NEW_LOAN) {
                    friendName = friendNameField.getText() == null || friendNameField.getText().isBlank()
                            ? null
                            : friendNameField.getText().trim();
                } else if (friendLoanOperation == FriendLoanOperation.REPAYMENT) {
                    final FriendLoanSummary selectedLoan = existingFriendLoanBox.getValue();
                    existingFriendLoanId = selectedLoan == null ? null : selectedLoan.getFriendLoanId();
                    friendName = selectedLoan == null ? null : selectedLoan.getFriendName();
                }
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                    || transactionType == null
                    || category == null
                    || date == null
                    || description.isEmpty()) {
                return null;
            }

            return new NewTransactionRequest(
                    amount,
                    transactionType,
                    category,
                    date,
                    description,
                    notes,
                    friendName,
                    friendLoanOperation,
                    existingFriendLoanId
            );
        });
    }

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

    private void openNewCategoryDialog() {
        final CategoryType selectedType = transactionTypeBox.getValue();
        if (selectedType == null) {
            return;
        }

        final NewCategoryDialog dialog = new NewCategoryDialog(selectedType);
        dialog.showAndWait().ifPresent(request -> {
            categoryCreator.apply(request, selectedType).ifPresent(createdCategory -> {
                updateCategoriesFor(selectedType);
                categoryBox.getSelectionModel().select(createdCategory);
            });
        });
    }

    private void updateCategoriesFor(final CategoryType selectedType) {
        final List<Category> filteredCategories = categoryCatalog.getActiveCategories().stream()
                .filter(category -> category.getType() == selectedType)
                .toList();

        categoryBox.setItems(FXCollections.observableArrayList(filteredCategories));
        if (!filteredCategories.isEmpty()) {
            categoryBox.getSelectionModel().selectFirst();
        } else {
            categoryBox.getSelectionModel().clearSelection();
        }
    }

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

    private static String formatCategoryType(final CategoryType type) {
        return switch (type) {
            case INCOME -> "Income";
            case EXPENSE -> "Expense";
            case TRANSFER -> "Transfer";    
            case FRIEND_LOAN -> "FRIEND LOAN";
        };
    }
}