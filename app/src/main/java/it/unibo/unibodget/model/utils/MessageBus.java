package it.unibo.unibodget.model.utils;

import it.unibo.unibodget.model.utils.event.MessageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class MessageBus {

    private static final MessageBus instance = new MessageBus();
    private final Map<Class<?>, List<Consumer<?>>> subscribers = new HashMap<>();

    private MessageBus() {}

    public static <T extends MessageEvent> void subscribe(Class<T> type, Consumer<T> subscriber) {
        instance.subscribers.computeIfAbsent(type, k -> new ArrayList<>()).add(subscriber);
    }

    public static void send(MessageEvent msg) {
        var classType = msg.getClass();
        var subs = instance.subscribers.get(classType);
        if (subs == null) {
            return;
        }
        for (var sub : subs) {
            ((Consumer<MessageEvent>)sub).accept(msg);
        }
    }

}
