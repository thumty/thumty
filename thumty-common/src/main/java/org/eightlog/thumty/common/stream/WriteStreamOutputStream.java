package org.eightlog.thumty.common.stream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    private final ByteBuf buffer;

    private final long timeout;

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
            synchronized (this) {
                this.notify();
            }
        });

        this.buffer = Unpooled.buffer(bufferSize);

        this.timeout = timeUnit.toMillis(timeout);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        try {
            if (writeStream.writeQueueFull()) {
                if (timeout > 0) {
                    this.wait(timeout);
                } else {
                    this.wait();
                }
            }

            if (!buffer.isWritable(len)) {
                flush();
            }

            buffer.writeBytes(b, off, len);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        try {
            if (writeStream.writeQueueFull()) {
                this.wait(timeout);

                if (writeStream.writeQueueFull()) {
                    throw new IOException("Write stream timeout, no data could be written in " + timeout + " milliseconds");
                }
            }

            if (!buffer.isWritable()) {
                flush();
            }

            buffer.writeByte(b);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        if (buffer.isReadable()) {
            byte[] buf = new byte[buffer.readableBytes()];

            buffer.readBytes(buf);
            buffer.discardSomeReadBytes();

            writeStream.write(Buffer.buffer(buf));
        }
    }

    @Override
    public void close() throws IOException {
        flush();

        writeStream.end();
        buffer.release();
    }
}
