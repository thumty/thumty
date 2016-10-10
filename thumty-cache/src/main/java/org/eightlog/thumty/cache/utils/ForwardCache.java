package org.eightlog.thumty.cache.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.eightlog.thumty.cache.Cache;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A special cache implementation that forwards all requests to delegated future cache.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ForwardCache<T extends Serializable> implements Cache<T> {

    private final Future<Cache<T>> delegate;

    public ForwardCache(Future<Cache<T>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void getIfPresent(String key, Handler<AsyncResult<T>> handler) {
        delegate.compose(cache -> cache.getIfPresent(key)).setHandler(handler);
    }

    @Override
    public void put(String key, T value, Handler<AsyncResult<Void>> handler) {
        delegate.compose(cache -> cache.put(key, value)).setHandler(handler);
    }

    @Override
    public void put(String key, T value, LocalDateTime expires, Handler<AsyncResult<Void>> handler) {
        delegate.compose(cache -> cache.put(key, value, expires)).setHandler(handler);
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
        delegate.compose(Cache::cleanUp).setHandler(handler);
    }
}
