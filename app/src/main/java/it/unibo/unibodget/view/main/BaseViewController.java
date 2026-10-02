package it.unibo.unibodget.view.main;

import it.unibo.unibodget.model.behavioral.Disposable;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.MessageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The base view class that implement the Disposable pattern.
 * Useful for the pub-sub pattern. The dispose method is already implemented, need just to call it.
 * The onDispose method is a Template Method pattern, if the user needs to do something on dispose event,
 * the user should override the method with his custom calls.
 */
public class BaseViewController implements Disposable {

    private final List<Consumer<?>> subscribers = new ArrayList<>();

    /**
     * Just a mediator between the user and the MessageBus pattern class.
     * Made for commodity, so the user could not think about the message bus and save the tokens/consumer refs.
     *
     * @param eventType The type of the event.
     * @param subscriber The callback method of the interested instance
     * @param <T> A custom-made event that implement the {@link MessageEvent} marker interface
     */
    protected <T extends MessageEvent> void subscribe(
            final Class<T> eventType,
            final Consumer<T> subscriber
    ) {
        subscribers.add(MessageBus.subscribe(eventType, subscriber));
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        MessageBus.unsubscribeAll(subscribers);
        subscribers.clear();
        onDispose();
    }

    /**
     * Template-method hook called at the end of {@link #dispose()}.
     * Override to perform custom cleanup logic when this controller is destroyed.
     */
    protected void onDispose() { }
}
