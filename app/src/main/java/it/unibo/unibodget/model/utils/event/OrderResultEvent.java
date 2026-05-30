package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.investment.OrderResult;

/**
 * An event that carries the result of an investment order operation.
 * 
 * @param result the outcome of the order execution
 */
public record OrderResultEvent(OrderResult result) implements MessageEvent { }
