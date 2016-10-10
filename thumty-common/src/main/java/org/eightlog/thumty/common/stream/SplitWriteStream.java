package org.eightlog.thumty.common.stream;

import io.vertx.core.Handler;
import io.vertx.core.streams.WriteStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A {@link WriteStream} splitter.
 * <p>
 * A {@link WriteStream} that output to multiply write streams.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class SplitWriteStream<T> implements WriteStream<T> {

    private final List<WriteStream<T>> streams;

    private Handler<Throwable> exceptionHandler;

    private Handler<Void> drainHandler;

    /**
     * Constructs new {@code SplitWriteStream} with multiply output write streams
     *
     * @param streams the collection of write streams
     */
    @SafeVarargs
    public SplitWriteStream(WriteStream<T>... streams) {
        this(Arrays.asList(streams));
    }

    /**
     * Constructs new {@code SplitWriteStream} with multiply output write streams
     *
     * @param streams the collection write streams
     */
    public SplitWriteStream(Collection<WriteStream<T>> streams) {
        this(new ArrayList<>(streams));
    }

    /**
     * Constructs new {@code SplitWriteStream} with multiply output write streams
     *
     * @param streams the collection write streams
     */
    public SplitWriteStream(List<WriteStream<T>> streams) {
        this.streams = streams;

        for (WriteStream<T> stream : this.streams) {
            stream.exceptionHandler(this::fail);
            stream.drainHandler(this::drain);
        }
    }

    @Override
    public WriteStream<T> exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @Override
    public WriteStream<T> write(T data) {
        for (WriteStream<T> stream : streams) {
            stream.write(data);
        }
        return this;
    }

    @Override
    public void end() {
        for (WriteStream<T> stream : streams) {
            stream.end();
        }
    }

    @Override
    public WriteStream<T> setWriteQueueMaxSize(int maxSize) {
        for (WriteStream<T> stream : streams) {
            stream.setWriteQueueMaxSize(maxSize);
        }
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        for (WriteStream<T> stream : streams) {
            if (stream.writeQueueFull()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public WriteStream<T> drainHandler(Handler<Void> handler) {
        this.drainHandler = handler;
        return this;
    }

    private void drain(Void v) {
        if (drainHandler != null) {
            drainHandler.handle(v);
        }
    }

    private void fail(Throwable throwable) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(throwable);
        }
    }
}
