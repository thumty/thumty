package org.eightlog.thumty.common.stream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The {@link ReadStream} {@link InputStream} wrapper.
 * <p>
 * Wraps buffer read stream to input stream.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ReadStreamInputStream extends InputStream {

    /**
     * Read queue size, optimized for AsyncFile default buffer size
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Event timeout in milliseconds
     */
    private final long timeout;

    /**
     * Internal byte buffer
     */
    private final BlockingDeque<Byte> buffer;

    private final ReadStream<Buffer> readStream;

    /**
     * Synchronization lock
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Stream event condition
     */
    private final Condition event = lock.newCondition();

    /**
     * Stream end indicator
     */
    private final AtomicBoolean finished = new AtomicBoolean(false);

    /**
     * Stream error
     */
    private final AtomicReference<Throwable> error = new AtomicReference<>(null);

    /**
     * Create input stream with default buffer and pooling interval
     *
     * @param readStream the read stream
     */
    public ReadStreamInputStream(ReadStream<Buffer> readStream) {
        this(readStream, BUFFER_SIZE, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Create input stream with default pooling interval
     *
     * @param readStream the read stream
     * @param bufferSize the buffer size
     */
    public ReadStreamInputStream(ReadStream<Buffer> readStream, int bufferSize) {
        this(readStream, bufferSize, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Create input stream with default buffer size
     *
     * @param readStream the read stream
     * @param timeout    the pooling timeout
     * @param timeUnit   the pooling timeout time unit
     */
    public ReadStreamInputStream(ReadStream<Buffer> readStream, long timeout, TimeUnit timeUnit) {
        this(readStream, BUFFER_SIZE, timeout, timeUnit);
    }

    /**
     * ReadStream input stream constructor
     *
     * @param readStream the read stream
     * @param bufferSize the buffer size
     * @param timeout    the pooling timeout
     * @param timeUnit   the pooling timeout time unit
     */
    public ReadStreamInputStream(ReadStream<Buffer> readStream, int bufferSize, long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(readStream, "readStream must not be null");

        if (bufferSize < 0) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }

        if (timeout < 0 || timeUnit == null) {
            throw new IllegalArgumentException("Invalid timeout");
        }

        this.timeout = timeUnit.toNanos(timeout);

        this.buffer = new LinkedBlockingDeque<>();

        this.readStream = readStream;

        this.readStream.handler(buf -> {
            int i = 0;

            // Read bytes to buffer
            while (i < buf.length()) {
                buffer.offerFirst(buf.getByte(i));
                i++;
            }

            if (buffer.size() >= bufferSize) {
                // Pause if there are remains
                readStream.pause();
            }

            lock.lock();
            try {
                event.signal();
            } finally {
                lock.unlock();
            }
        });

        this.readStream.endHandler(end -> {
            finished.set(true);

            lock.lock();
            try {
                event.signal();
            } finally {
                lock.unlock();
            }
        });

        this.readStream.exceptionHandler(err -> {
            error.set(err);

            lock.lock();
            try {
                event.signal();
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public int read() throws IOException {
        try {
            return readInternal();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private int readInternal() throws IOException, InterruptedException {
        Byte b = buffer.pollLast();
        boolean empty = b == null;

        if (empty) {
            // Already finished
            if (finished.get()) {
                return -1;
            }

            // Has error
            if (error.get() != null) {
                throw new IOException(error.get());
            }

            // Resume reading
            readStream.resume();

            lock.lock();
            try {
                if (timeout > 0) {
                    if (event.awaitNanos(timeout) <= 0) {
                        throw new IOException("Read stream timeout, no data arrived in " + timeout + " nanoseconds");
                    }
                } else {
                    event.await();
                }
            } finally {
                lock.unlock();
            }

            b = buffer.pollLast();

            if (b == null) {
                // Finished after resume
                if (finished.get()) {
                    return -1;
                }

                // Error after resume
                if (error.get() != null) {
                    throw new IOException(error.get());
                }

                throw new IllegalStateException("Unexpected event sequence");
            }
        }

        return Byte.toUnsignedInt(b);
    }

}
