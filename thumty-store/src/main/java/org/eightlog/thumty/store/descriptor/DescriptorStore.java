package org.eightlog.thumty.store.descriptor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface DescriptorStore {

    default Future<Long> create(String id, Descriptor descriptor) {
        Future<Long> future = Future.future();
        create(id, descriptor, future.completer());
        return future;
    }

    default Future<Descriptor> read(String id) {
        Future<Descriptor> future = Future.future();
        read(id, future.completer());
        return future;
    }

    default Future<Boolean> exists(String id) {
        Future<Boolean> future = Future.future();
        exists(id, future.completer());
        return future;
    }

    default Future<Long> delete(String id) {
        Future<Long> future = Future.future();
        delete(id, future.completer());
        return future;
    }

    default Future<Long> duplicate(String id) {
        Future<Long> future = Future.future();
        duplicate(id, future.completer());
        return future;
    }

    /**
     * Write descriptor to store
     *
     * @param id         the file id
     * @param descriptor the descriptor
     * @param handler    the result handler
     */
    void create(String id, Descriptor descriptor, Handler<AsyncResult<Long>> handler);

    /**
     * Increase descriptor reference count
     *
     * @param id      the content id
     * @param handler the result handler of newly reference count
     */
    void duplicate(String id, Handler<AsyncResult<Long>> handler);

    /**
     * Read descriptor from store
     *
     * @param id      the file id
     * @param handler the result handler
     */
    void read(String id, Handler<AsyncResult<Descriptor>> handler);

    /**
     * Check descriptor existence
     *
     * @param id      the file id
     * @param handler the result handler
     */
    void exists(String id, Handler<AsyncResult<Boolean>> handler);

    /**
     * Delete file descriptor
     *
     * @param id      the file id
     * @param handler the result handler
     */
    void delete(String id, Handler<AsyncResult<Long>> handler);

}
