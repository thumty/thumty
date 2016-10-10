package org.eightlog.thumty.cache.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import org.eightlog.thumty.cache.ContentCache;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.time.LocalDateTime;

/**
 * A special content cache implementation that forwards all requests to delegated future cache.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ForwardContentCache implements ContentCache {

    private final Future<ContentCache> delegate;

    public ForwardContentCache(Future<ContentCache> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void getIfPresent(String key, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        delegate.compose(cache -> cache.getIfPresent(key)).setHandler(handler);
    }

    @Override
    public void put(String key, ReadStream<Buffer> content, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        content.pause();
        delegate.compose(cache -> cache.put(key, content)).setHandler(handler);
    }

    @Override
    public void put(String key, ReadStream<Buffer> content, LocalDateTime expires, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        content.pause();
        delegate.compose(cache -> cache.put(key, content, expires)).setHandler(handler);
    }

    @Override
    public void put(String source, String target, LocalDateTime expires, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        delegate.compose(cache -> cache.put(source, target, expires)).setHandler(handler);
    }

    @Override
    public void invalidate(String key, Handler<AsyncResult<Void>> handler) {
        delegate.compose(cache -> cache.invalidate(key)).setHandler(handler);
    }

    @Override
    public void invalidateAll(Iterable<String> keys, Handler<AsyncResult<Void>> handler) {
        delegate.compose(cache -> cache.invalidateAll(keys)).setHandler(handler);
    }

    @Override
    public void cleanUp(Handler<AsyncResult<Void>> handler) {
        delegate.compose(ContentCache::cleanUp).setHandler(handler);
    }
}
