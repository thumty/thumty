package org.eightlog.thumty.cache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.Shareable;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Asynchronous cache.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface Cache<T extends Serializable> extends Shareable {

    default Future<T> getIfPresent(String key) {
        Future<T> result = Future.future();
        getIfPresent(key, result.completer());
        return result;
    }

    default Future<Void> put(String key, T value) {
        Future<Void> result = Future.future();
        put(key, value, result.completer());
        return result;
    }

    default Future<Void> put(String key, T value, LocalDateTime expires) {
        Future<Void> result = Future.future();
        put(key, value, expires, result.completer());
        return result;
    }

    default Future<Void> invalidate(String key) {
        Future<Void> result = Future.future();
        invalidate(key, result.completer());
        return result;
    }

    default Future<Void> invalidateAll(Iterable<String> keys) {
        Future<Void> result = Future.future();
        invalidateAll(keys, result.completer());
        return result;
    }

    default Future<Void> cleanUp() {
        Future<Void> result = Future.future();
        cleanUp(result.completer());
        return result;
    }

    /**
     * Get record if present
     *
     * @param key     the cache key
     * @param handler the record handler, if record doesn't exists the value will be null
     */
    void getIfPresent(String key, Handler<AsyncResult<T>> handler);

    /**
     * Put value to cache
     *
     * @param key     the cache key
     * @param value the value
     * @param handler the record handler
     */
    void put(String key, T value, Handler<AsyncResult<Void>> handler);

    /**
     * Put value to cache with optional expiration
     *
     * @param key     the cache key
     * @param value the value
     * @param expires optional expiration time
     * @param handler the record handler
     */
    void put(String key, T value, LocalDateTime expires, Handler<AsyncResult<Void>> handler);

    /**
     * Invalidates cache value
     *
     * @param key     the key
     * @param handler the handler
     */
    void invalidate(String key, Handler<AsyncResult<Void>> handler);

    /**
     * Invalidates all records with given keys
     *
     * @param keys    the record keys
     * @param handler the handler
     */
    void invalidateAll(Iterable<String> keys, Handler<AsyncResult<Void>> handler);

    /**
     * Clean up cache
     *
     * @param handler the handler
     */
    void cleanUp(Handler<AsyncResult<Void>> handler);
}
