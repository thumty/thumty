package org.eightlog.thumty.store.binary;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface BinaryStore {

    default Future<Binary> create(ReadStream<Buffer> content) {
        Future<Binary> result = Future.future();
        create(content, result.completer());
        return result;
    }

    default Future<Binary> duplicate(String id) {
        Future<Binary> result = Future.future();
        duplicate(id, result.completer());
        return result;
    }

    default Future<ReadStream<Buffer>> read(String id) {
        Future<ReadStream<Buffer>> result = Future.future();
        read(id, result.completer());
        return result;
    }

    default Future<Void> delete(String id) {
        Future<Void> result = Future.future();
        delete(id, result.completer());
        return result;
    }

    default Future<Boolean> exists(String id) {
        Future<Boolean> result = Future.future();
        exists(id, result.completer());
        return result;
    }

    default Future<Binary> get(String id) {
        Future<Binary> result = Future.future();
        get(id, result.completer());
        return result;
    }

    /**
     * Store content in storage
     *
     * @param content the content
     * @param handler the handler
     */
    void create(ReadStream<Buffer> content, Handler<AsyncResult<Binary>> handler);

    /**
     * Create content duplicate
     *
     * @param id      the content id
     * @param handler the result handler
     */
    void duplicate(String id, Handler<AsyncResult<Binary>> handler);

    /**
     * Read stored content
     *
     * @param id      the content id
     * @param handler the content handler
     */
    void read(String id, Handler<AsyncResult<ReadStream<Buffer>>> handler);

    /**
     * Check whether content with id exists
     *
     * @param id      the content id
     * @param handler the result handler
     */
    void exists(String id, Handler<AsyncResult<Boolean>> handler);

    /**
     * Delete content from store by id
     *
     * @param id      the content id
     * @param handler the result handler
     */
    void delete(String id, Handler<AsyncResult<Void>> handler);

    /**
     * Read content info by id
     *
     * @param id      the content id
     * @param handler the result handler
     */
    void get(String id, Handler<AsyncResult<Binary>> handler);
}
