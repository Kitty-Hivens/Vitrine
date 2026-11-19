package me.jellysquid.mods.sodium.common.util.collections;

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

public class FutureDequeDrain<T> implements Iterator<T> {
    private final Deque<CompletableFuture<T>> deque;
    private final Predicate<Throwable> recover;
    private T next = null;

    public FutureDequeDrain(Deque<CompletableFuture<T>> deque) {
        this(deque, failure -> false);
    }

    /**
     * @param recover Offered the cause of every future that completed exceptionally. Returning true skips that
     *                future as if it had been cancelled; returning false rethrows.
     */
    public FutureDequeDrain(Deque<CompletableFuture<T>> deque, Predicate<Throwable> recover) {
        this.deque = deque;
        this.recover = recover;
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        }

        findNext();

        return next != null;
    }

    private void findNext() {
        while (!deque.isEmpty()) {
            CompletableFuture<T> future = deque.remove();

            try {
                next = future.join();
                return;
            } catch (CancellationException e) {
                // no-op
            } catch (CompletionException e) {
                if (!this.recover.test(e.getCause())) {
                    throw e;
                }
            }
        }
    }
    
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        T result = next;
        next = null;

        return result;
    }
}
