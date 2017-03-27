package org.eightlog.thumty.common.stream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * The {@link ReadStream} {@link InputStream} wrapper.
 * <p>
 * Wraps buffer read stream to input stream.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ReadStreamInputStream extends InputStream {

    private final Vertx vertx;

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
    private final ByteBuf buffer;

    private final ReadStream<Buffer> readStream;

    /**
     * Stream end indicator
     */
    private boolean finished = false;

    /**
     * Stream error
     */
    private Throwable error = null;

    /**
     * Create input stream with default buffer and pooling interval
     *
     * @param readStream the read stream
     */
    public ReadStreamInputStream(Vertx vertx, ReadStream<Buffer> readStream) {
        this(vertx, readStream, BUFFER_SIZE, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Create input stream with default pooling interval
     *
     * @param readStream the read stream
     * @param bufferSize the buffer size
     */
    public ReadStreamInputStream(Vertx vertx, ReadStream<Buffer> readStream, int bufferSize) {
        this(vertx, readStream, bufferSize, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Create input stream with default buffer size
     *
     * @param readStream the read stream
     * @param timeout    the pooling timeout
     * @param timeUnit   the pooling timeout time unit
     */
    public ReadStreamInputStream(Vertx vertx, ReadStream<Buffer> readStream, long timeout, TimeUnit timeUnit) {
        this(vertx, readStream, BUFFER_SIZE, timeout, timeUnit);
    }

    /**
     * ReadStream input stream constructor
     *
     * @param readStream the read stream
     * @param bufferSize the buffer size
     * @param timeout    the pooling timeout
     * @param timeUnit   the pooling timeout time unit
     */
    public ReadStreamInputStream(Vertx vertx, ReadStream<Buffer> readStream, int bufferSize, long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(readStream, "readStream must not be null");

        if (bufferSize < 0) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }

        if (timeout < 0 || timeUnit == null) {
            throw new IllegalArgumentException("Invalid timeout");
        }

        this.vertx = vertx;

        this.buffer = Unpooled.buffer(bufferSize);

        this.timeout = timeUnit.toMillis(timeout);

        this.readStream = readStream;

        // Set exception handler
        this.readStream.exceptionHandler(err -> {
            synchronized (this) {
                error = err;
                this.notify();
            }
        });

        // Set data handler
        this.readStream.handler(buf -> {
            synchronized (this) {
                if (!buffer.isWritable(buf.length())) {
                    buffer.discardSomeReadBytes();

                    if (!buffer.isWritable(buf.length())) {
                        buffer.capacity(buf.length() - buffer.writableBytes() + buffer.capacity());
                    }
                }

                buffer.writeBytes(buf.getByteBuf());

                if (buffer.readableBytes() >= bufferSize) {
                    // Pause if there are remains
                    readStream.pause();
                }

                this.notify();
            }
        });

        // Set end handler
        this.readStream.endHandler(end -> {
            synchronized (this) {
                finished = true;
                this.notify();
            }
        });
    }

    @Override
    public synchronized int read() throws IOException {
        // Check for error
        if (error != null) {
            throw new IOException(error);
        }

        try {
            if (!buffer.isReadable()) {
                if (finished) {
                    return -1;
                }

                // Resume read
                vertx.runOnContext(v -> {
                    if (!finished && error == null) {
                        readStream.resume();
                    }
                });

                this.wait(timeout);

                // Finished after resume
                if (finished) {
                    return -1;
                }

                // Error after resume
                if (error != null) {
                    throw new IOException(error);
                }

                if (!buffer.isReadable()) {
                    throw new IllegalStateException("Unexpected event sequence");
                }
            }

            return Byte.toUnsignedInt(buffer.readByte());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        // Check for error
        if (error != null) {
            throw new IOException(error);
        }

        int readable = buffer.readableBytes();
        int length = Math.min(readable, len);

        if (len == 0) {
            return 0;
        }

        if (readable == 0) {
            return super.read(b, off, len);
        }

        buffer.readBytes(b, off, length);
        return length;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        // Check for error
        if (error != null) {
            throw new IOException(error);
        }

        int skip = (int) n;
        int readable = buffer.readableBytes();

        if (readable >= skip) {
            buffer.skipBytes(skip);
            return skip;
        }

        if (readable > 0 && readable < skip) {
            buffer.skipBytes(readable);
            return readable;
        }

        return super.skip(n);
    }

    @Override
    public synchronized int available() throws IOException {
        return buffer.readableBytes();
    }

    @Override
    public void close() throws IOException {
        buffer.release();
    }
}
