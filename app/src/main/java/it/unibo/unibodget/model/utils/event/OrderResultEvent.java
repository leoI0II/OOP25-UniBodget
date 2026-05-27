package it.unibo.unibodget.model.utils.event;

import it.unibo.unibodget.model.investment.OrderResult;

public record OrderResultEvent(OrderResult result) implements MessageEvent {}
