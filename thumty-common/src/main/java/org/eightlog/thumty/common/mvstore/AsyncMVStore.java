package org.eightlog.thumty.common.mvstore;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;
import org.h2.mvstore.MVStore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class AsyncMVStore {

    private final Lock lock = new ReentrantLock();

    private final Vertx vertx;

    private MVStore store;

    private JsonObject config;

    public AsyncMVStore(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
    }

    public <K, V> Future<AsyncMVMap<K, V>> getMap(String name) {
        Future<AsyncMVMap<K, V>> future = Future.future();
        getMap(name, future.completer());
        return future;
    }

    public <K, V> void getMap(String name, Handler<AsyncResult<AsyncMVMap<K, V>>> handler) {
        ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();

        open(res -> {
            if (res.succeeded()) {
                MVStore mvStore = res.result();
                ctx.executeBlocking(future -> {
                    try {
                        future.complete(new AsyncMVMap<>(ctx, mvStore, mvStore.openMap(name)));
                    }catch (Throwable t) {
                        future.fail(t);
                    }
                }, handler);
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public <T> Future<T> execute(Function<MVStore, T> function) {
        Future<T> future = Future.future();
        execute(function, future.completer());
        return future;
    }

    public <T> void execute(Function<MVStore, T> function, Handler<AsyncResult<T>> handler) {
        ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();

        open(res -> {
            if (res.succeeded()) {
                MVStore store = res.result();
                ctx.executeBlocking(future -> {
                    try{
                        future.complete(function.apply(store));
                    } catch (Throwable t) {
                        future.fail(t);
                    }
                }, handler);
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public Future<Void> close() {
        Future<Void> future = Future.future();
        close(future.completer());
        return future;
    }

    public void close(Handler<AsyncResult<Void>> handler) {
        ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();

        if (store != null) {
            ctx.executeBlocking(future -> {
                lock.lock();
                try {
                    if (store != null) {
                        store.close();
                    }
                    future.complete();
                } catch (Throwable t) {
                    future.fail(t);
                } finally {
                    lock.unlock();
                }
            }, handler);
        } else {
            handler.handle(Future.succeededFuture());
        }
    }

    private void open(Handler<AsyncResult<MVStore>> handler) {
        ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();

        if (store != null) {
          handler.handle(Future.succeededFuture(store));
        } else {
            ctx.executeBlocking(future -> {
                lock.lock();
                try {
                    if (store == null) {
                        MVStore.Builder builder = new MVStore.Builder();

                        if (config.getString("fileName") != null) {
                            builder.fileName(config.getString("fileName"));
                        }

                        if (config.getInteger("autoCommitBufferSize") != null) {
                            builder.autoCommitBufferSize(config.getInteger("commitBufferSize") / 1024);
                        }

                        if (config.getBoolean("autoCommitDisabled") != null) {
                            builder.autoCommitDisabled();
                        }

//                        if (config.getInteger("autoCompactFillRate") != null) {
//                            builder.autoCompactFillRate(config.getInteger("autoCompactFillRate"));
//                        }


                        if (config.getBoolean("compress") != null){
                            builder.compress();
                        }

                        if (config.getBoolean("compressHigh") != null){
                            builder.compressHigh();
                        }

                        if (config.getBoolean("readOnly") != null){
                            builder.readOnly();
                        }

                        if (config.getInteger("cacheSize") != null){
                            builder.cacheSize(config.getInteger("cacheSize") / (1024 * 1024));
                        }

                        if (config.getInteger("pageSplitSize") != null) {
                            builder.pageSplitSize(config.getInteger("pageSplitSize") / 1024);
                        }

                        if (config.getString("encryptionKey") != null) {
                            builder.encryptionKey(config.getString("encryptionKey").toCharArray());
                        }

                        store = builder.open();
                    }
                    future.complete(store);
                }catch (Throwable t) {
                    future.fail(t);
                } finally {
                    lock.unlock();
                }
            }, handler);
        }
    }
}
