package net.adoptium.documentationservices.util;

import java.util.concurrent.locks.Lock;

/**
 * Provides method to sync calls based on a {@link Lock}
 */
public class SyncUtils {
    
    public static <T, E extends Throwable> T executeSynchronized(final Lock lock, final SupplierTask<T, E> task) throws E {
        lock.lock();
        try {
            return task.run();
        } finally {
            lock.unlock();
        }
    }

    public static <E extends Throwable> void executeSynchronized(final Lock lock, final Task<E> task) throws E {
        executeSynchronized(lock, () -> {
            task.run();
            return null;
        });
    }
}
