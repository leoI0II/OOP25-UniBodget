package it.unibo.unibodget.model.utils.event;

/**
 * Event to display an error notification in the main UI.
 *
 * @param msg the error message to show
 */
public record MainErrorNotificationEvent(String msg) implements MessageEvent { }
