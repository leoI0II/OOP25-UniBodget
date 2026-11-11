package it.unibo.unibodget.model.utils;

/**
 * Heartbeat of the application: while it is started, it sends a {@link
 * it.unibo.unibodget.model.utils.event.TickEvent} on the {@link MessageBus} at a fixed interval,
 * so the classes that need to do something periodically (refresh prices, save snapshots, ...) only
 * subscribe to the event and do not create their own timers.
 *
 * <p>The implementation decides how the time is measured (e.g. a JavaFX timeline in the view),
 * this interface keeps the model free from it.
 */
public interface AppTicker {

    /** Starts sending the ticks, the first one is sent right away. Does nothing if already started. */
    void start();

    /** Stops sending the ticks. */
    void stop();
}
