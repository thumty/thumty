package org.eightlog.thumty.store.binary.utils;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.eightlog.thumty.store.Attributes;

import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class StreamProxy {

    private final static Tika TIKA = new Tika(MimeTypes.getDefaultMimeTypes());
    private final static int TIKA_BUFFER_SIZE = 4096;

    private Hasher hasher;
    private Buffer tikaBuffer;
    private long size = 0;

    private Handler<Attributes> endHandler;
    private Handler<Throwable> exceptionHandler;

    public static StreamProxy create(ReadStream<Buffer> rs, WriteStream<Buffer> ws) {
        return new StreamProxy().proxy(rs, ws);
    }

    public StreamProxy() {
        this.hasher = Hashing.sha1().newHasher();
        this.tikaBuffer = Buffer.buffer(TIKA_BUFFER_SIZE);
    }

    public StreamProxy endHandler(final Handler<Attributes> endHandler) {
        Objects.requireNonNull(endHandler, "endHandler");
        this.endHandler = endHandler;
        return this;
    }

    public StreamProxy exceptionHandler(final Handler<Throwable> exceptionHandler) {
        Objects.requireNonNull(exceptionHandler, "exceptionHandler");
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public StreamProxy proxy(ReadStream<Buffer> rs, WriteStream<Buffer> ws) {
        size = 0;
        tikaBuffer = Buffer.buffer(TIKA_BUFFER_SIZE);
        hasher = Hashing.sha1().newHasher();

        rs.exceptionHandler(this::fail);
        rs.endHandler(this::end);

        rs.handler(buf -> {
            size += buf.length();
            hasher.putBytes(buf.getBytes());

            if (tikaBuffer.length() < TIKA_BUFFER_SIZE) {
                tikaBuffer.appendBuffer(buf);
            }

            ws.write(buf);

            if (ws.writeQueueFull()) {
                rs.pause();
                ws.drainHandler(v -> rs.resume());
            }
        });

        return this;
    }

    private void end(Void v) {
        if (endHandler != null) {
            endHandler.handle(new Attributes(hasher.hash().toString(), size, TIKA.detect(tikaBuffer.getBytes())));
        }
    }

    private void fail(Throwable throwable) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(throwable);
        }
    }
}
