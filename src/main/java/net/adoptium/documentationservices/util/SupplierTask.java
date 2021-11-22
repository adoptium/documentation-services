package net.adoptium.documentationservices.util;

public interface SupplierTask<T, E extends Throwable> {
    T run() throws E;
}
