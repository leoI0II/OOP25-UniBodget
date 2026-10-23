package it.unibo.unibodget.view.utils;

import it.unibo.unibodget.model.utils.AppTicker;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.TickEvent;
import java.time.LocalDateTime;
import java.util.Objects;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * {@link AppTicker} made with a JavaFX {@link Timeline}.
 *
 * <p>The timeline does not create a new thread: the ticks are sent on the JavaFX Application
 * Thread, the same of the UI, so the subscribers can read and change the model and the view
 * without concurrency problems. For the same reason they must do only short work.
 */
public final class FxAppTicker implements AppTicker {

    private final Timeline timeline;

    /**
     * Creates the ticker, it does not start until {@link #start()}.
     *
     * @param interval the time between two ticks
     */
    public FxAppTicker(final java.time.Duration interval) {
        Objects.requireNonNull(interval);
        timeline =
                new Timeline(
                        // the keyframe at zero sends a tick at the start of every cycle (so also
                        // right after play())
                        new KeyFrame(Duration.ZERO, e -> sendTick()),
                        // the second one only gives the length of the cycle
                        new KeyFrame(Duration.millis(interval.toMillis())));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        if (timeline.getStatus() != Animation.Status.RUNNING) {
            timeline.play();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        timeline.stop();
    }

    private static void sendTick() {
        MessageBus.send(new TickEvent(LocalDateTime.now()));
    }
}
