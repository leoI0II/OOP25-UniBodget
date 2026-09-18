package it.unibo.unibodget.view.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Utility class for showing toast notifications in the bottom-right corner of a window.
 * Toasts appear with a fade-in animation, stay visible for a short duration, then fade out.
 */
public final class ToastNotification {

    /**
     * Semantic type of a toast notification, each carrying a background color.
     */
    public enum Type {
        /** Operation completed successfully. */
        SUCCESS("#4CAF50"),
        /** An error occurred. */
        ERROR("#F44336"),
        /** A non-critical warning. */
        WARNING("#FF9800"),
        /** General informational message. */
        INFO("#2196F3");

        private final String color;

        Type(final String color) {
            this.color = color;
        }

        /**
         * Returns the CSS hex color string for this type.
         *
         * @return the background color, e.g. {@code "#4CAF50"}
         */
        public String getColor() {
            return color;
        }
    }

    private static final double DEFAULT_DURATION_SECONDS = 3;
    private static final double FADE_DURATION_MS = 300;
    private static final int MARGIN = 20;

    private ToastNotification() { }

    /**
     * Shows a success toast in the bottom-right corner of {@code window}.
     *
     * @param window  the owner window
     * @param message the text to display
     */
    public static void showSuccess(final Window window, final String message) {
        show(window, message, Type.SUCCESS);
    }

    /**
     * Shows an error toast in the bottom-right corner of {@code window}.
     *
     * @param window  the owner window
     * @param message the text to display
     */
    public static void showError(final Window window, final String message) {
        show(window, message, Type.ERROR);
    }

    /**
     * Shows a warning toast in the bottom-right corner of {@code window}.
     *
     * @param window  the owner window
     * @param message the text to display
     */
    public static void showWarning(final Window window, final String message) {
        show(window, message, Type.WARNING);
    }

    /**
     * Shows an info toast in the bottom-right corner of {@code window}.
     *
     * @param window  the owner window
     * @param message the text to display
     */
    public static void showInfo(final Window window, final String message) {
        show(window, message, Type.INFO);
    }

    /**
     * Shows a toast with the default duration ({@value DEFAULT_DURATION_SECONDS} seconds).
     *
     * @param window  the owner window
     * @param message the text to display
     * @param type    the semantic type controlling the background color
     */
    public static void show(final Window window, final String message, final Type type) {
        show(window, message, type, DEFAULT_DURATION_SECONDS);
    }

    /**
     * Shows a toast with a custom visible duration.
     *
     * <p>The toast fades in, stays visible for {@code durationSeconds}, then fades out
     * and hides itself automatically.</p>
     *
     * @param window          the owner window used to anchor the popup position
     * @param message         the text to display
     * @param type            the semantic type controlling the background color
     * @param durationSeconds how long the toast stays fully visible before fading out
     */
    public static void show(
            final Window window,
            final String message,
            final Type type,
            final double durationSeconds
    ) {
        final Label label = buildLabel(message, type);
        final Popup popup = new Popup();
        popup.getContent().add(label);
        popup.setAutoHide(true);

        popup.setOnShown(e -> {
            popup.setX(window.getX() + window.getWidth() - popup.getWidth() - MARGIN);
            popup.setY(window.getY() + window.getHeight() - popup.getHeight() - MARGIN);
        });

        popup.show(window);

        final var fadeIn = new FadeTransition(Duration.millis(FADE_DURATION_MS), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        final var pause = new PauseTransition(Duration.seconds(durationSeconds));

        final var fadeOut = new FadeTransition(Duration.millis(FADE_DURATION_MS * 2), label);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> popup.hide());

        new SequentialTransition(fadeIn, pause, fadeOut).play();
    }

    private static Label buildLabel(final String message, final Type type) {
        final Label label = new Label(message);
        label.setStyle(
                "-fx-background-color: "
                        + type.getColor()
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-padding: 12 16 12 16;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 13;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        return label;
    }
}
