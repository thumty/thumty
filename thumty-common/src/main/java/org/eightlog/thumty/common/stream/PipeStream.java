package org.eightlog.thumty.common.stream;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

/**
 * {@code PipeStream} is pipe between {@link WriteStream} and {@link ReadStream}
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class PipeStream<T> {

    /**
     * The write stream queue size, after which write stream becomes full. It's a soft limit.
     */
    private static final int QUEUE_SIZE = 512;

    private boolean paused;
    private boolean ended;

    private PipeReadStream<T> readStream;

    private PipeWriteStream<T> writeStream;

    private BlockingDeque<T> queue;

    /**
     * Construct new pipe
     */
    public PipeStream() {
        this.queue = new LinkedBlockingDeque<>();
        this.readStream = new PipeReadStream<>(this);
        this.writeStream = new PipeWriteStream<>(this);
    }

    /**
     * Create new pipe and provide writeStream stream and readStream stream handlers
     *
     * @param ws  the writeStream stream handler
     * @param rs  the readStream stream handler
     * @param <T> the type of pipe
     */
    public static <E, T> Future<E> pipe(Function<WriteStream<T>, Future<Void>> ws, Function<ReadStream<T>, Future<E>> rs) {
        return new PipeStream<T>().handle(ws, rs);
    }

    /**
     * Pipe stream handler
     *
     * @param ws the writeStream stream handler
     * @param rs the readStream stream handler
     */
    public void handle(Handler<WriteStream<T>> ws, Handler<ReadStream<T>> rs) {
        ws.handle(writeStream);
        rs.handle(readStream);
    }

    public <E> Future<E> handle(Function<WriteStream<T>, Future<Void>> ws, Function<ReadStream<T>, Future<E>> rs) {
        return CompositeFuture.all(ws.apply(writeStream), rs.apply(readStream)).map(f -> f.resultAt(1));
    }

    private void flush() {
        boolean isFull = queue.size() >= QUEUE_SIZE;

        while (!paused && !queue.isEmpty()) {
            handleData(queue.pollLast());
        }

        if (isFull && queue.size() < QUEUE_SIZE) {
            handleDrain();
        }

        if (ended && queue.isEmpty()) {
            handleEnd();
        }
    }

    private void handleData(T t) {
        if (readStream.handler != null) {
            try {
                readStream.handler.handle(t);
            } catch (Throwable e) {
                handleWriteException(e);
            }
        }
    }

    private void handleEnd() {
        if (readStream.endHandler != null) {
            try {
                readStream.endHandler.handle((Void) null);
            } catch (Throwable e) {
                handleWriteException(e);
            }
        }
    }

    private void handleDrain() {
        if (writeStream.drainHandler != null) {
            try {
                writeStream.drainHandler.handle((Void) null);
            } catch (Throwable e) {
                handleReadException(e);
            }
        }
    }

    private void handleReadException(Throwable t) {
        if (readStream.exceptionHandler != null) {
            readStream.exceptionHandler.handle(t);
        }
    }

    private void handleWriteException(Throwable t) {
        if (writeStream.exceptionHandler != null) {
            writeStream.exceptionHandler.handle(t);
        }
    }

    private static class PipeWriteStream<T> implements WriteStream<T> {
        private final PipeStream<T> coordinator;

        private Handler<Throwable> exceptionHandler;
        private Handler<Void> drainHandler;

        public PipeWriteStream(PipeStream<T> coordinator) {
            this.coordinator = coordinator;
        }

        @Override
        public WriteStream<T> exceptionHandler(Handler<Throwable> handler) {
            this.exceptionHandler = handler;
            return this;
        }

        @Override
        public WriteStream<T> write(T data) {
            coordinator.queue.offerFirst(data);
            coordinator.flush();
            return this;
        }

        @Override
        public void end() {
            coordinator.ended = true;
            coordinator.flush();
        }

        @Override
        public WriteStream<T> setWriteQueueMaxSize(int maxSize) {
            return this;
        }

        @Override
        public boolean writeQueueFull() {
            return coordinator.queue.size() >= QUEUE_SIZE;
        }

        @Override
        public WriteStream<T> drainHandler(Handler<Void> handler) {
            this.drainHandler = handler;
            return this;
        }
    }

    private static class PipeReadStream<T> implements ReadStream<T> {
        private final PipeStream<T> coordinator;

        private Handler<Throwable> exceptionHandler;
        private Handler<T> handler;
        private Handler<Void> endHandler;

        public PipeReadStream(PipeStream<T> coordinator) {
            this.coordinator = coordinator;
        }

        @Override
        public ReadStream<T> exceptionHandler(Handler<Throwable> handler) {
            this.exceptionHandler = handler;
            return this;
        }

        @Override
        public ReadStream<T> handler(Handler<T> handler) {
            this.handler = handler;
            return this;
        }

        @Override
        public ReadStream<T> pause() {
            coordinator.paused = true;
            return this;
        }

        @Override
        public ReadStream<T> resume() {
            coordinator.paused = false;
            coordinator.flush();
            return this;
        }

        @Override
        public ReadStream<T> endHandler(Handler<Void> endHandler) {
            this.endHandler = endHandler;
            return this;
        }
    }
}
