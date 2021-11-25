package net.adoptium.documentationservices.util;

/**
 * Functional interface like {@link Runnable} but with the support to throw an exception.
 *
 * @param <E> exception type
 */
@FunctionalInterface
public interface Task<E extends Throwable> {
    void run() throws E;
}
