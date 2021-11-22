package net.adoptium.documentationservices.util;

/**
 * Functional interface like {@link java.util.function.Supplier} but with the support to throw an exception.
 *
 * @param <T> return type
 * @param <E> exception type
 */
@FunctionalInterface
public interface SupplierTask<T, E extends Throwable> {
    T run() throws E;
}
