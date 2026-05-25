package it.unibo.unibodget.model.utils.event;

/**
 * Marker interface for all events published through {@link it.unibo.unibodget.model.utils.MessageBus}.
 *
 * <p>Implement this interface to define a new event type. Records are recommended:</p>
 * <pre>{@code
 * public record BudgetCreated(BigDecimal amount) implements MessageEvent {}
 * }</pre>
 */
public interface MessageEvent {}