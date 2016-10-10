package org.eightlog.thumty.common.stream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class WriteStreamOutputStream extends OutputStream {

    /**
     * Default write buffer size, optimized for AsyncFile
     */
    private final static int BUFFER_SIZE = 8192;

    /**
     * The write stream
     */
    private final WriteStream<Buffer> writeStream;

    private final Lock lock = new ReentrantLock();
    private final Condition open = lock.newCondition();

    private final Buffer buffer;
    private final long timeout;
    private int length;
    private int pos = 0;

    /**
     * Create write stream output stream with default buffer size and timeout
     *
     * @param writeStream the write stream
     */
    public WriteStreamOutputStream(WriteStream<Buffer> writeStream) {
        this(writeStream, BUFFER_SIZE, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Create write stream output stream with default buffer size
     *
     * @param writeStream the write stream
     * @param timeout     the write timeout
     * @param timeUnit    the write timeout units
     */
    public WriteStreamOutputStream(WriteStream<Buffer> writeStream, long timeout, TimeUnit timeUnit) {
        this(writeStream, BUFFER_SIZE, timeout, timeUnit);
    }

    /**
     * Create write stream output stream with default write timeout
     *
     * @param writeStream the write stream
     * @param bufferSize  the buffer size
     */
    public WriteStreamOutputStream(WriteStream<Buffer> writeStream, int bufferSize) {
        this(writeStream, bufferSize, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Create write stream output stream
     *
     * @param writeStream the target write stream
     * @param bufferSize  the buffer size
     * @param timeout     the write timeout, 0 for indefinitely await
     * @param timeUnit    the write timeout units
     */
    public WriteStreamOutputStream(WriteStream<Buffer> writeStream, int bufferSize, long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(writeStream, "writeStream must not be null");

        if (bufferSize < 0) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }

        if (timeout < 0 || timeUnit == null) {
            throw new IllegalArgumentException("Invalid timeout");
        }

        this.writeStream = writeStream;
        this.writeStream.drainHandler(v -> {
            lock.lock();
            try {
                open.signal();
            } finally {
                lock.unlock();
            }
        });

        this.length = bufferSize;
        this.buffer = Buffer.buffer(length);

        this.timeout = timeUnit.toNanos(timeout);
    }

    @Override
    public void write(int b) throws IOException {
        try {
            if (writeStream.writeQueueFull()) {
                lock.lock();
                try {
                    if (timeout > 0) {
                        if (open.awaitNanos(timeout) <= 0) {
                            throw new IOException("Write stream timeout, no data could be written in " + timeout + " nanoseconds");
                        }
                    } else {
                        open.await();
                    }
                } finally {
                    lock.unlock();
                }
            }

            buffer.setByte(pos++, (byte) b);

            if (pos >= length) {
                flush();
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        if (pos > 0) {
            writeStream.write(buffer.slice(0, pos).copy());
            pos = 0;
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        writeStream.end();
    }
}
