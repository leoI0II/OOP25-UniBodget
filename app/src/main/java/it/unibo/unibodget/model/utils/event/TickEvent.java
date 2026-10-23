package it.unibo.unibodget.model.utils.event;

import java.time.LocalDateTime;

/**
 * Sent at a fixed interval by the {@link it.unibo.unibodget.model.utils.AppTicker} while the app
 * is running, like a heartbeat.
 *
 * <p>Each subscriber decides by itself how often to work, comparing {@code time} with the last
 * time it did something (the ticks are not precise, so they must not be counted).
 *
 * @param time the moment of the tick
 */
public record TickEvent(LocalDateTime time) implements MessageEvent { }
