package org.eightlog.thumty.store.content;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import org.eightlog.thumty.store.binary.Binary;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class BinaryContent implements Content {

    private final Binary binary;

    private final String path;

    private final ContentAttributes attributes;

    public BinaryContent(Binary binary, String path, ContentAttributes attributes) {
        this.binary = binary;
        this.path = path;
        this.attributes = attributes;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public ContentAttributes getAttributes() {
        return attributes;
    }

    @Override
    public BinaryContent exceptionHandler(Handler<Throwable> handler) {
        binary.exceptionHandler(handler);
        return this;
    }

    @Override
    public BinaryContent handler(Handler<Buffer> handler) {
        binary.handler(handler);
        return this;
    }

    @Override
    public BinaryContent pause() {
        binary.pause();
        return this;
    }

    @Override
    public BinaryContent resume() {
        binary.resume();
        return this;
    }

    @Override
    public BinaryContent endHandler(Handler<Void> endHandler) {
        binary.endHandler(endHandler);
        return this;
    }
}
