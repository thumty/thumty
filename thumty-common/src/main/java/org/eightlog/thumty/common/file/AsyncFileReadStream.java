package org.eightlog.thumty.common.file;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.streams.ReadStream;

/**
 * One way file read stream, closes file after end of read.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class AsyncFileReadStream implements ReadStream<Buffer> {

    private final AsyncFile file;

    private Handler<Void> endHandler;

    /**
     * Create new {@code AsyncFileReadStream}
     *
     * @param file the source file
     */
    public AsyncFileReadStream(AsyncFile file) {
        this.file = file;
        this.file.endHandler(this::end);
    }

    @Override
    public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        file.exceptionHandler(handler);
        return this;
    }

    @Override
    public ReadStream<Buffer> handler(Handler<Buffer> handler) {
        file.handler(handler);
        return this;
    }

    @Override
    public ReadStream<Buffer> pause() {
        file.pause();
        return this;
    }

    @Override
    public ReadStream<Buffer> resume() {
        file.resume();
        return this;
    }

    @Override
    public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    private void end(Void v) {
        if (endHandler != null) {
            endHandler.handle(v);
        }
        file.close();
    }
}
