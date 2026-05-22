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

    public enum Type {
        SUCCESS("#4CAF50"),
        ERROR("#F44336"),
        WARNING("#FF9800"),
        INFO("#2196F3");

        private final String color;

        Type(final String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    private static final double DEFAULT_DURATION_SECONDS = 2.5;
    private static final double FADE_DURATION_MS = 200;
    private static final int MARGIN = 20;

    // utility class — no instantiation
    private ToastNotification() {}

    public static void showSuccess(final Window window, final String message) {
        show(window, message, Type.SUCCESS);
    }

    public static void showError(final Window window, final String message) {
        show(window, message, Type.ERROR);
    }

    public static void showWarning(final Window window, final String message) {
        show(window, message, Type.WARNING);
    }

    public static void showInfo(final Window window, final String message) {
        show(window, message, Type.INFO);
    }

    public static void show(final Window window, final String message, final Type type) {
        show(window, message, type, DEFAULT_DURATION_SECONDS);
    }

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
                "-fx-background-color: " + type.getColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 16 12 16;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        return label;
    }
}
