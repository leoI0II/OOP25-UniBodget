package it.unibo.unibodget.model.behavioral;

/**
 * Implemented by objects that hold resources (subscriptions, listeners, references)
 * that must be released when the object is no longer needed.
 */
@FunctionalInterface
public interface Disposable {

    /**
     * Releases all resources held by this object.
     * After this call the object should not be used again.
     */
    void dispose();
}
