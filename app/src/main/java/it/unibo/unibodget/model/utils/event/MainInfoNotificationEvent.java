package it.unibo.unibodget.model.utils.event;

/**
 * Event to display an informational notification in the main UI.
 *
 * @param msg the message to show
 */
public record MainInfoNotificationEvent(String msg) implements MessageEvent { }
