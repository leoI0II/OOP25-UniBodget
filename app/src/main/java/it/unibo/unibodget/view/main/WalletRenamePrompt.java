package it.unibo.unibodget.view.main;

import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

/**
 * Asks the user for a new wallet name until it is accepted or the user cancels.
 *
 * <p>The naming rules live in the model: when the rename action rejects the name with an {@link
 * IllegalArgumentException}, the error is shown in a popup and the dialog is opened again with the
 * rejected text, so the user can fix it or cancel.
 */
public final class WalletRenamePrompt {

    private WalletRenamePrompt() {
        // utility class
    }

    /**
     * Shows {@code dialog} until {@code renameAction} accepts the typed name or the user cancels.
     * The typed name is trimmed before being passed on.
     *
     * @param dialog the already configured input dialog (title, texts, owner)
     * @param renameAction applies the new name; throws {@link IllegalArgumentException} if the name
     *     breaks the naming rules
     */
    public static void ask(final TextInputDialog dialog, final Consumer<String> renameAction) {
        while (true) {
            final Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return; // cancel/esc/win closed
            }
            final String typedName = result.get();
            try {
                renameAction.accept(typedName.trim());
                return;
            } catch (final IllegalArgumentException e) {
                showInvalidName(dialog, e.getMessage());
                dialog.getEditor().setText(typedName);
            }
        }
    }

    private static void showInvalidName(final TextInputDialog dialog, final String message) {
        final Alert error = new Alert(Alert.AlertType.ERROR);
        error.initOwner(dialog.getOwner());
        error.setTitle("Invalid name");
        error.setHeaderText("Invalid wallet name");
        error.setContentText(message + "\nPlease try again.");
        error.showAndWait();
    }
}
