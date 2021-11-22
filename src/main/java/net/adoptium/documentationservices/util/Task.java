package net.adoptium.documentationservices.util;

public interface Task<E extends Throwable> {
    void run() throws E;
}
