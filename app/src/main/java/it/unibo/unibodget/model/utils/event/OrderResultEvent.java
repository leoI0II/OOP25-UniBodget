package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.investment.OrderResult;

public class OrderResultEvent implements MessageEvent {
    public final OrderResult result;
    public OrderResultEvent(final OrderResult result) {
        this.result = result;
    }
}
