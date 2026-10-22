package it.unibo.unibodget.view.utils;

import it.unibo.unibodget.model.settings.ThemeManager;
import java.io.File;
import java.util.Optional;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Utility helper class centralizing JavaFX dialog, alert, and file chooser interactions.
 */
public final class DialogHelper {

    private DialogHelper() {
        // utility class
    }

    /**
     * Resolves the JavaFX {@link Window} hosting the given node, if attached to a scene.
     *
     * @param node the UI node
     * @return     an optional containing the host window, or empty if detached
     */
    public static Optional<Window> findWindow(final Node node) {
        return Optional.ofNullable(node)
                .map(Node::getScene)
                .map(Scene::getWindow);
    }

    /**
     * Displays a themed alert modal of the given type to the specified owner window.
     * 
     * @param ownerWindow the parent window, or null if none
     * @param type        the type of alert (e.g., WARNING, ERROR, INFORMATION)
     * @param title       the alert dialog title
     * @param header      the alert dialog header
     * @param content     the alert dialog content
     */
    private static void showAlert(
            final Window ownerWindow,
            final Alert.AlertType type,
            final String title,
            final String header,
            final String content) {
        final Alert alert = new Alert(type);
        if (ownerWindow != null) {
            alert.initOwner(ownerWindow);
        }
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ThemeManager.applyThemeToDialog(alert);
        alert.showAndWait();
    }

    /**
     * Displays a themed warning alert modal to the window of the given node.
     *
     * @param ownerNode       the node whose window owns the alert
     * @param title           the alert dialog title
     * @param header          the alert dialog header
     * @param content         the alert dialog content
     */
    public static void showWarning(
            final Node ownerNode, final String title, final String header, final String content) {
        showWarning(findWindow(ownerNode).orElse(null), title, header, content);
    }

    /**
     * Displays a themed warning alert modal to the given owner window.
     *
     * @param ownerWindow     the parent window, or null if none
     * @param title           the alert dialog title
     * @param header          the alert dialog header
     * @param content         the alert dialog content
     */
    public static void showWarning(
            final Window ownerWindow, final String title, final String header, final String content) {
        showAlert(ownerWindow, Alert.AlertType.WARNING, title, header, content);
    }

    /**
     * Displays a themed error alert modal to the window of the given node.
     *
     * @param ownerNode       the node whose window owns the alert
     * @param title           the alert dialog title
     * @param header          the alert dialog header
     * @param content         the alert dialog content
     */
    public static void showError(
            final Node ownerNode, final String title, final String header, final String content) {
        showError(findWindow(ownerNode).orElse(null), title, header, content);
    }

    /**
     * Displays a themed error alert modal to the given owner window.
     *
     * @param ownerWindow     the parent window, or null if none
     * @param title           the alert dialog title
     * @param header          the alert dialog header
     * @param content         the alert dialog content
     */
    public static void showError(
            final Window ownerWindow, final String title, final String header, final String content) {
        showAlert(ownerWindow, Alert.AlertType.ERROR, title, header, content);
    }

    /**
     * Displays a themed information alert modal to the window of the given node.
     *
     * @param ownerNode       the node whose window owns the alert
     * @param title           the alert dialog title
     * @param header          the alert dialog header
     * @param content         the alert dialog content
     */
    public static void showInfo(
            final Node ownerNode, final String title, final String header, final String content) {
        showInfo(findWindow(ownerNode).orElse(null), title, header, content);
    }

    /**
     * Displays a themed information alert modal to the given owner window.
     *
     * @param ownerWindow     the parent window, or null if none
     * @param title           the alert dialog title
     * @param header          the alert dialog header
     * @param content         the alert dialog content
     */
    public static void showInfo(
            final Window ownerWindow, final String title, final String header, final String content) {
        showAlert(ownerWindow, Alert.AlertType.INFORMATION, title, header, content);
    }

    /**
     * Displays a themed confirmation dialog and returns whether the user confirmed (OK).
     *
     * @param ownerNode       the node whose window owns the dialog
     * @param title           the dialog title
     * @param message         the confirmation question text
     * @return                true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(
            final Node ownerNode, final String title, final String message) {
        return showConfirmation(ownerNode, title, null, message);
    }

    /**
     * Displays a themed confirmation dialog and returns whether the user confirmed (OK).
     *
     * @param ownerNode the node whose window owns the dialog
     * @param title           the dialog title
     * @param header          the dialog header
     * @param content         the confirmation question text
     * @return                true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(
            final Node ownerNode, final String title, final String header, final String content) {
        final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        findWindow(ownerNode).ifPresent(confirm::initOwner);
        confirm.setTitle(title);
        confirm.setHeaderText(header);
        confirm.setContentText(content);
        ThemeManager.applyThemeToDialog(confirm);
        return confirm.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    /**
     * Opens a save file chooser dialog preconfigured for CSV exports.
     *
     * @param ownerNode       the node whose window owns the file chooser
     * @param title           the dialog title
     * @param defaultFileName the proposed initial file name
     * @return                an optional containing the chosen file, or empty if cancelled
     */
    public static Optional<File> showSaveCsvDialog(
            final Node ownerNode, final String title, final String defaultFileName) {
        return showSaveCsvDialog(findWindow(ownerNode).orElse(null), title, defaultFileName);
    }

    /**
     * Opens a save file chooser dialog preconfigured for CSV exports.
     *
     * @param ownerWindow     the parent window, or null if unattached
     * @param title           the dialog title
     * @param defaultFileName the proposed initial file name
     * @return                an optional containing the chosen file, or empty if cancelled
     */
    public static Optional<File> showSaveCsvDialog(
            final Window ownerWindow, final String title, final String defaultFileName) {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        final String safeName = defaultFileName != null
                ? defaultFileName.replaceAll("\\s+", "_")
                : "data";
        chooser.setInitialFileName(safeName.endsWith(".csv") ? safeName : safeName + ".csv");
        final File downloads = new File(System.getProperty("user.home"), "Downloads");
        if (downloads.isDirectory()) {
            chooser.setInitialDirectory(downloads);
        }
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        final File file = ownerWindow instanceof Stage stage
                ? chooser.showSaveDialog(stage)
                : chooser.showSaveDialog(null);
        return Optional.ofNullable(file);
    }

}
