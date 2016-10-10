package org.eightlog.thumty.cache.content;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import org.eightlog.thumty.store.Attributes;
import org.eightlog.thumty.store.ExpirableAttributedContent;
import org.eightlog.thumty.store.content.Content;

import java.time.LocalDateTime;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class BinaryExpirableAttributedContent implements ExpirableAttributedContent {

    private final Content content;
    private final LocalDateTime expires;

    public BinaryExpirableAttributedContent(Content content) {
        this(null, content);
    }

    public BinaryExpirableAttributedContent(LocalDateTime expires, Content content) {
        this.content = content;
        this.expires = expires;
    }

    @Override
    public Attributes getAttributes() {
        return content.getAttributes();
    }

    @Override
    public LocalDateTime getExpires() {
        return expires;
    }

    @Override
    public BinaryExpirableAttributedContent exceptionHandler(Handler<Throwable> handler) {
        content.exceptionHandler(handler);
        return this;
    }

    @Override
    public BinaryExpirableAttributedContent handler(Handler<Buffer> handler) {
        content.handler(handler);
        return this;
    }

    @Override
    public BinaryExpirableAttributedContent pause() {
        content.pause();
        return this;
    }

    @Override
    public BinaryExpirableAttributedContent resume() {
        content.resume();
        return this;
    }

    @Override
    public BinaryExpirableAttributedContent endHandler(Handler<Void> endHandler) {
        content.endHandler(endHandler);
        return this;
    }
}
