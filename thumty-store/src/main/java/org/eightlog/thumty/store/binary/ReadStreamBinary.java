package org.eightlog.thumty.store.binary;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import org.eightlog.thumty.store.Attributes;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ReadStreamBinary implements Binary {

    private final BinaryStore binaryStore;

    private final String id;

    private final Attributes attributes;

    private boolean loading;

    private ReadStream<Buffer> stream;

    private Handler<Buffer> handler;

    private Handler<Throwable> exceptionHandler;

    private Handler<Void> endHandler;

    private boolean paused;

    /**
     * Constructs base {@link Binary} implementation
     *
     * @param binaryStore the backend binary store
     * @param id          the binary identifier
     * @param attributes  the binary attributes
     */
    public ReadStreamBinary(BinaryStore binaryStore, String id, Attributes attributes) {
        this.binaryStore = binaryStore;
        this.id = id;
        this.attributes = attributes;
    }

    /**
     * @return a binary content unique id
     */
    public String getId() {
        return id;
    }

    /**
     * @return a binary content attributes
     */
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public ReadStreamBinary exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        load();
        return this;
    }

    @Override
    public ReadStreamBinary handler(Handler<Buffer> handler) {
        this.handler = handler;
        load();
        return this;
    }

    @Override
    public ReadStreamBinary pause() {
        if (stream != null) {
            stream.pause();
        } else {
            paused = true;
        }
        return this;
    }

    @Override
    public ReadStreamBinary resume() {
        if (stream != null) {
            stream.resume();
        } else {
            paused = false;
        }
        return this;
    }

    @Override
    public ReadStreamBinary endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        load();
        return this;
    }

    private synchronized void load() {
        if (!loading) {
            loading = true;
            binaryStore.read(id, this::onLoad);
        }
    }

    private synchronized void onLoad(AsyncResult<ReadStream<Buffer>> res) {
        if (res.succeeded()) {
            stream = res.result().handler(this::handle).exceptionHandler(this::fail).endHandler(this::end);

            if (paused) {
                stream.pause();
            }
        } else {
            fail(res.cause());
        }
    }

    private void end(Void v) {
        if (endHandler != null) {
            endHandler.handle(v);
        }
    }

    private void handle(Buffer buf) {
        if (handler != null) {
            handler.handle(buf);
        }
    }

    private void fail(Throwable cause) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(cause);
        }
    }
}
