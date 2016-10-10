package org.eightlog.thumty.common.stream;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link ReadStream} splitter.
 * <p>
 * Splits {@link ReadStream} that it can be handled by several parallel handler
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class SplitReadStream<T> implements ReadStream<T> {

    private final Lock lock = new ReentrantLock();

    private final ReadStream<T> readStream;

    private final List<Handler<Throwable>> exceptionHandlers = new ArrayList<>();
    private final List<Handler<T>> handlers = new ArrayList<>();
    private final List<Handler<Void>> endHandlers = new ArrayList<>();

    private final int numberOfParallelHandlers;

    /**
     * Construct new {@code SplitReadStream}.
     *
     * Will pause read stream until number of data handlers is lower than {@code numberOfParallelHandlers}
     *
     * @param readStream               the source read stream
     * @param numberOfParallelHandlers number of parallel readers
     */
    public SplitReadStream(ReadStream<T> readStream, int numberOfParallelHandlers) {
        this.readStream = readStream;
        this.numberOfParallelHandlers = numberOfParallelHandlers;

        // Pause read stream, wait for data handlers
        this.readStream.pause();

        this.readStream.handler(t -> {
            lock.lock();
            try {
                for (Handler<T> handler : handlers) {
                    handler.handle(t);
                }
            } finally {
                lock.unlock();
            }
        });

        this.readStream.exceptionHandler(e -> {
            lock.lock();
            try {
                for (Handler<Throwable> handler : exceptionHandlers) {
                    handler.handle(e);
                }
            } finally {
                lock.unlock();
            }
        });

        this.readStream.endHandler(v -> {
            lock.lock();
            try {
                for (Handler<Void> handler : endHandlers) {
                    handler.handle(v);
                }
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public ReadStream<T> exceptionHandler(Handler<Throwable> handler) {
        lock.lock();
        try {
            exceptionHandlers.add(handler);
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public ReadStream<T> handler(Handler<T> handler) {
        lock.lock();
        try {
            handlers.add(handler);

            if (handlers.size() == numberOfParallelHandlers) {
                readStream.resume();
            }

            if (handlers.size() > numberOfParallelHandlers) {
                throw new IllegalStateException("More than " + numberOfParallelHandlers + " data handlers had been set");
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public ReadStream<T> pause() {
        readStream.pause();
        return this;
    }

    @Override
    public ReadStream<T> resume() {
        lock.lock();
        try {
            if (handlers.size() >= numberOfParallelHandlers) {
                readStream.resume();
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public ReadStream<T> endHandler(Handler<Void> endHandler) {
        lock.lock();
        try {
            endHandlers.add(endHandler);
        } finally {
            lock.unlock();
        }
        return this;
    }
}
