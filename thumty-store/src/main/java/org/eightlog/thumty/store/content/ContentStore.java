package org.eightlog.thumty.store.content;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ContentStore {

    default Future<Content> write(String path, @Nullable JsonObject meta, ReadStream<Buffer> stream) {
        Future<Content> future = Future.future();
        write(path, meta, stream, future.completer());
        return future;
    }

    default Future<Content> write(String path, ReadStream<Buffer> stream) {
        return write(path, null, stream);
    }

    default Future<Content> read(String path) {
        Future<Content> future = Future.future();
        read(path, future.completer());
        return future;
    }

    default Future<Boolean> exists(String path) {
        Future<Boolean> future = Future.future();
        exists(path, future.completer());
        return future;
    }

    default Future<Content> copy(String from, String to) {
        Future<Content> future = Future.future();
        copy(from, to, future.completer());
        return future;
    }

    default Future<Content> copy(String from, String to, JsonObject meta) {
        Future<Content> future = Future.future();
        copy(from, to, meta, future.completer());
        return future;
    }

    default Future<Void> delete(String path) {
        Future<Void> future = Future.future();
        delete(path, future.completer());
        return future;
    }

    default Future<ContentAttributes> attributes(String path) {
        Future<ContentAttributes> future = Future.future();
        attributes(path, future.completer());
        return future;
    }

    /**
     * Alias persist without expiration and meta
     *
     * @param path    unique content path
     * @param stream  the content stream
     * @param handler the result handler
     */
    default void write(String path, ReadStream<Buffer> stream, Handler<AsyncResult<Content>> handler) {
        write(path, null, stream, handler);
    }

    /**
     * Persist read stream, with optional meta information.
     * If content with given path already exists, it will be overridden.
     *
     * @param path    unique content path
     * @param meta    optional content meta information
     * @param stream  the content stream
     * @param handler the result handler
     */
    void write(String path, @Nullable JsonObject meta, ReadStream<Buffer> stream,
               Handler<AsyncResult<Content>> handler);

    /**
     * Get content at path
     *
     * @param path    the content path
     * @param handler the content handler
     */
    void read(String path, Handler<AsyncResult<Content>> handler);

    /**
     * Check whether path exists
     *
     * @param path    the content path
     * @param handler the result handler
     */
    void exists(String path, Handler<AsyncResult<Boolean>> handler);

    /**
     * Delete content at path
     *
     * @param path    the content path
     * @param handler the result handler
     */
    void delete(String path, Handler<AsyncResult<Void>> handler);

    /**
     * Create copy of the path, replacing destination if it exists.
     *
     * @param from    the source
     * @param to      the destination
     * @param handler the result handler
     */
    void copy(String from, String to, Handler<AsyncResult<Content>> handler);

    /**
     * Create copy of the path, overriding meta information, replacing destination if it exists.
     *
     * @param from    the source
     * @param to      the destination
     * @param meta    update meta information
     * @param handler the result handler
     */
    void copy(String from, String to, JsonObject meta, Handler<AsyncResult<Content>> handler);

    /**
     * Get content attributes by path
     *
     * @param path    the content path
     * @param handler the result handler
     */
    void attributes(String path, Handler<AsyncResult<ContentAttributes>> handler);
}
