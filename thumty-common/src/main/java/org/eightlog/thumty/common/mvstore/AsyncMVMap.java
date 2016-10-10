package org.eightlog.thumty.common.mvstore;

import io.vertx.core.*;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.ContextInternal;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class AsyncMVMap<K, V> {

    private final WorkerExecutor executor;

    private final MVStore mvStore;

    private final MVMap<K, V> mvMap;

    public AsyncMVMap(Context context, MVStore mvStore, MVMap<K, V> mvMap) {
        this.executor = ((ContextInternal)context).createWorkerExecutor();
        this.mvStore = mvStore;
        this.mvMap = mvMap;
    }

    public AsyncMVMap(Vertx vertx, MVStore mvStore, MVMap<K, V> mvMap) {
        this.executor = ((ContextImpl)vertx.getOrCreateContext()).createWorkerExecutor();
        this.mvStore = mvStore;
        this.mvMap = mvMap;
    }

    /**
     * Get the key value or null if not exists
     * @param key the key
     * @param handler the value handler
     */
    public void get(K key, Handler<AsyncResult<V>> handler) {
        execute(()-> mvMap.get(key), handler);
    }

    public Future<V> get(K key) {
        Future<V> future = Future.future();
        get(key, future.completer());
        return future;
    }

    public void remove(K key, Handler<AsyncResult<V>> handler) {
        execute(()-> mvMap.remove(key), handler);
    }

    public Future<V> remove(K key) {
        Future<V> future = Future.future();
        remove(key, future.completer());
        return future;
    }

    public void put(K key, V value, Handler<AsyncResult<V>> handler) {
        execute(()-> mvMap.put(key, value), handler);
    }

    public Future<V> put(K key, V value) {
        Future<V> future = Future.future();
        put(key, value, future.completer());
        return future;
    }

    /**
     * Commit pending changes
     * @param handler the version handler
     */
    public void commit(Handler<AsyncResult<Long>> handler) {
        execute(mvStore::commit, handler);
    }

    public void rollback(Handler<AsyncResult<Void>> handler) {
        executeVoid(mvStore::rollback, handler);
    }

    public <T> Future<T> execute(Function<MVMap<K, V>, T> func) {
        Future<T> future = Future.future();
        execute(func, future.completer());
        return future;
    }

    public <T> void execute(Function<MVMap<K, V>, T> func, Handler<AsyncResult<T>> handler) {
        executor.executeBlocking(future -> {
            try {
                future.complete(func.apply(mvMap));
            }catch (Throwable t){
                future.fail(t);
            }
        }, handler);
    }

    private <T> void execute(Supplier<T> supplier, Handler<AsyncResult<T>> handler) {
        executor.executeBlocking(future -> {
            try {
                future.complete(supplier.get());
            }catch (Throwable t){
                future.fail(t);
            }
        }, handler);
    }

    private void executeVoid(SupplierVoid supplier, Handler<AsyncResult<Void>> handler) {
        executor.executeBlocking(future -> {
            try {
                supplier.get();
                future.complete();
            }catch (Throwable t){
                future.fail(t);
            }
        }, handler);
    }

    private interface SupplierVoid {
        void get();
    }

}
