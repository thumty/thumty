package org.eightlog.thumty.cache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.streams.ReadStream;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.time.LocalDateTime;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ContentCache extends Shareable {

    /**
     * {@code getIfPresent} future wrapper
     *
     * @param key the cache key
     * @return a cached entry future
     */
    default Future<ExpirableAttributedContent> getIfPresent(String key) {
        Future<ExpirableAttributedContent> result = Future.future();
        getIfPresent(key, result.completer());
        return result;
    }

    default Future<ExpirableAttributedContent> put(String key, ReadStream<Buffer> content) {
        Future<ExpirableAttributedContent> result = Future.future();
        put(key, content, result.completer());
        return result;
    }

    default Future<ExpirableAttributedContent> put(String key, ReadStream<Buffer> content, LocalDateTime expires) {
        Future<ExpirableAttributedContent> result = Future.future();
        put(key, content, expires, result.completer());
        return result;
    }

    default Future<ExpirableAttributedContent> put(String source, String target, LocalDateTime expires) {
        Future<ExpirableAttributedContent> result = Future.future();
        put(source, target, expires, result.completer());
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
    void getIfPresent(String key, Handler<AsyncResult<ExpirableAttributedContent>> handler);

    /**
     * Put buffer stream to content cache
     *
     * @param key     the cache key
     * @param content the content
     * @param handler the record handler
     */
    void put(String key, ReadStream<Buffer> content, Handler<AsyncResult<ExpirableAttributedContent>> handler);

    /**
     * Put buffer stream to content cache with optional expiration
     *
     * @param key     the cache key
     * @param content the content
     * @param expires optional expiration time
     * @param handler the record handler
     */
    void put(String key, ReadStream<Buffer> content, LocalDateTime expires, Handler<AsyncResult<ExpirableAttributedContent>> handler);

    /**
     * Put new record in cache based on existing one
     *
     * @param source  the source record key
     * @param target  the target record key
     * @param expires optional expiration time of new record
     * @param handler the record handler
     */
    void put(String source, String target, LocalDateTime expires, Handler<AsyncResult<ExpirableAttributedContent>> handler);

    /**
     * Invalidates cache record
     *
     * @param key     the record key
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
