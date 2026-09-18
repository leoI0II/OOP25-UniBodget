package it.unibo.unibodget.model.utils;

import it.unibo.unibodget.model.utils.event.MessageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A static facade implementing the Publisher-Subscriber pattern.
 *
 * <p>Acts as a central message bus: publishers send events without knowing
 * who is listening; subscribers register interest in specific event types
 * without knowing who publishes them.</p>
 *
 * <p>All events must implement {@link MessageEvent}. The bus routes each
 * event to its subscribers using the runtime class of the event as key.</p>
 *
 * <p>Usage example:
 * <pre>{@code
 * // Subscribe — store the returned token for later unsubscription
 * Consumer<BudgetCreated> token = MessageBus.subscribe(BudgetCreated.class, this::onBudgetCreated);
 *
 * // Publish
 * MessageBus.send(new BudgetCreated(amount));
 *
 * // Unsubscribe (e.g. in destroy())
 * MessageBus.unsubscribe(BudgetCreated.class, token);
 * }</pre>
 * </p>
 *
 * <p><b>Important:</b> avoid passing anonymous lambdas to {@code subscribe} if you
 * plan to unsubscribe later — lambdas cannot be compared by reference.
 * Always use method references ({@code this::method}) or store the returned
 * token and pass it to {@link #unsubscribe}.</p>
 */
public final class MessageBus {

    private static final MessageBus INSTANCE = new MessageBus();
    private final Map<Class<?>, List<Consumer<?>>> subscribers = new HashMap<>();

    private MessageBus() { }

    /**
     * Registers a subscriber for a specific event type.
     *
     * <p>The returned {@link Consumer} is the subscription token. Store it
     * if you need to unsubscribe later (e.g. when the subscribing object
     * is destroyed).</p>
     *
     * @param <T>        the event type
     * @param type       the {@link Class} object representing the event type
     * @param subscriber the consumer to invoke when an event of {@code type} is sent
     * @return the subscriber itself, to be used as a token for {@link #unsubscribe}
     */
    public static <T extends MessageEvent> Consumer<T> subscribe(
            final Class<T> type,
            final Consumer<T> subscriber
    ) {
        INSTANCE.subscribers.computeIfAbsent(type, k -> new ArrayList<>()).add(subscriber);
        return subscriber;
    }

    /**
     * Publishes an event to all subscribers registered for its runtime type.
     *
     * <p>The event is routed using {@link Object#getClass()}, so the exact
     * runtime class must match the class used during {@link #subscribe}.</p>
     *
     * <p>If no subscribers are registered for the event type, the call is a no-op.</p>
     *
     * @param msg the event to publish; must implement {@link MessageEvent}
     */
    public static void send(final MessageEvent msg) {
        final var classType = msg.getClass();
        final var subs = INSTANCE.subscribers.get(classType);
        if (subs == null) {
            return;
        }
        for (final var sub : subs) {
            ((Consumer<MessageEvent>) sub).accept(msg);
        }
    }

    /**
     * Removes a specific subscriber for a given event type.
     *
     * @param <T>        the event type
     * @param event      the {@link Class} object representing the event type
     * @param subscriber the consumer token returned by {@link #subscribe}
     * @return {@code true} if the subscriber was found and removed,
     *         {@code false} if it wasn't registered
     */
    public static <T extends MessageEvent> boolean unsubscribe(
            final Class<T> event,
            final Consumer<T> subscriber
    ) {
        if (INSTANCE.subscribers.containsKey(event)) {
            return INSTANCE.subscribers.get(event).remove(subscriber);
        }
        return false;
    }

    /**
     * Removes a batch of subscribers across all event types.
     *
     * <p>Intended for use in {@code destroy()} methods, where an object
     * needs to unregister all its subscriptions at once:</p>
     * <pre>{@code
     * private final List<Consumer<?>> tokens = new ArrayList<>();
     *
     * public void init() {
     *     tokens.add(MessageBus.subscribe(BudgetCreated.class, this::onBudgetCreated));
     *     tokens.add(MessageBus.subscribe(TransactionAdded.class, this::onTransactionAdded));
     * }
     *
     * public void destroy() {
     *     MessageBus.unsubscribeAll(tokens);
     * }
     * }</pre>
     *
     * @param subscribers the list of consumer tokens to remove
     */
    public static void unsubscribeAll(final List<Consumer<?>> subscribers) {
        for (final var subscriber : subscribers) {
            for (final var entry : INSTANCE.subscribers.entrySet()) {
                entry.getValue().remove(subscriber);
            }
        }
    }
}
