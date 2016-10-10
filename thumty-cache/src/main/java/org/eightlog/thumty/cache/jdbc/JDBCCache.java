package org.eightlog.thumty.cache.jdbc;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.jdbc.JDBCClient;
import org.eightlog.thumty.cache.Cache;
import org.eightlog.thumty.common.jdbc.JDBCTransaction;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;

import javax.annotation.Nullable;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCCache<T extends Serializable> implements Cache<T> {

    private final JDBCWrapper client;

    private final String table;

    private final long cacheSize;

    private final long expiresAfterAccess;

    private final long expiresAfterWrite;

    public JDBCCache(JDBCClient client, String table, long cacheSize, long expiresAfterWrite, long expiresAfterAccess) {
        this.client = JDBCWrapper.create(client);
        this.table = table;
        this.cacheSize = cacheSize;
        this.expiresAfterWrite = expiresAfterWrite;
        this.expiresAfterAccess = expiresAfterAccess;
    }

    public JDBCCache(JDBCClient client, String table) {
        this(client, table, 0, 0, 0);
    }

    @Override
    public void getIfPresent(String key, Handler<AsyncResult<T>> handler) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(handler);

        // Update access time when possible
        touch(key);

        client.begin(tx -> read(tx, key)).setHandler(handler);
    }

    @Override
    public void put(String key, T value, Handler<AsyncResult<Void>> handler) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(handler);

        put(key, value, expiration(null), handler);
    }

    @Override
    public void put(String key, T value, LocalDateTime expires, Handler<AsyncResult<Void>> handler) {
        LocalDateTime expiration = expiration(expires);

        client.begin(tx ->
                exists(tx, key)
                        .compose(exists -> {
                            if (exists) {
                                return update(tx, key, value, expiration);
                            } else {
                                return insert(tx, key, value, expiration);
                            }
                        })
                        .compose(tx::commit)
        ).setHandler(handler);
    }

    @Override
    public void invalidate(String key, Handler<AsyncResult<Void>> handler) {
        client.begin(tx -> delete(tx, key).compose(tx::commit)).setHandler(handler);
    }

    @Override
    public void invalidateAll(Iterable<String> keys, Handler<AsyncResult<Void>> handler) {
        List<Future> futures = new ArrayList<>();

        for (String key : keys) {
            futures.add(invalidate(key));
        }

        CompositeFuture.join(futures).map((Void) null).setHandler(handler);
    }

    @Override
    public void cleanUp(Handler<AsyncResult<Void>> handler) {
        Future<Void> clearExpired = client.begin(tx -> tx.update("DELETE FROM " + table + " WHERE expires < NOW()").map((Void) null).compose(tx::commit));
        Future<Void> clearSize;
        Future<Void> clearAccessed;

        if (cacheSize > 0) {
            clearSize = client.begin(tx -> size(tx)
                    .compose(size -> {
                        if (size > cacheSize) {
                            return tx.update("DELETE FROM " + table + " WHERE key IN (SELECT key FROM " + table + " ORDER BY accessed ASC LIMIT " + (size - cacheSize) + ")").map((Void) null);
                        } else {
                            return Future.succeededFuture();
                        }
                    })
                    .compose(tx::commit)
            );
        } else {
            clearSize = Future.succeededFuture();
        }

        if (expiresAfterAccess > 0) {
            Date timestamp = Date.from(
                    LocalDateTime.now()
                            .minus(expiresAfterAccess, ChronoUnit.MILLIS)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );

            clearAccessed = client.begin(tx -> tx.update("DELETE FROM " + table + " WHERE accessed < ?", timestamp).compose(tx::commit)).map((Void)null);
        } else {
            clearAccessed = Future.succeededFuture();
        }

        CompositeFuture.join(clearExpired, clearSize, clearAccessed).map((Void) null).setHandler(handler);
    }

    /**
     * Insert new key value
     *
     * @param tx      the transaction
     * @param key     the key
     * @param t   the value
     * @param expires the expiration time
     * @return a result future
     */
    private Future<Void> insert(JDBCTransaction tx, String key, T t, LocalDateTime expires) {
        Date expireDate = expires != null ? Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()) : null;
        try {
            return tx.update("INSERT INTO " + table + " (key, value, accessed, expires) VALUES (?, ?, NOW(), ?)", key, serialize(t), expireDate).map((Void) null);
        }catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    /**
     * Update key value
     *
     * @param tx      the transaction
     * @param key     the key
     * @param t   the value
     * @param expires the expiration time
     * @return a result future
     */
    private Future<Void> update(JDBCTransaction tx, String key, T t, LocalDateTime expires) {
        Date expireDate = expires != null ? Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()) : null;
        try {
            return tx.update("UPDATE " + table + " SET accessed = NOW(), expires = ?, value = ? WHERE key = ?", expireDate, serialize(t), key)
                    .map((Void) null);
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    /**
     * Delete value
     *
     * @param tx  the transaction
     * @param key the cache key
     * @return the result future
     */
    private Future<Void> delete(JDBCTransaction tx, String key) {
        return tx.update("DELETE FROM " + table + " WHERE key = ?", key).map((Void) null);
    }

    /**
     * Read value for key
     *
     * @param tx  the transaction
     * @param key the cache key
     * @return a json object value
     */
    private Future<T> read(JDBCTransaction tx, String key) {
        return tx.query("SELECT value FROM " + table + " WHERE key = ? AND (expires IS NULL OR expires > NOW()) LIMIT 1", key)
                .compose(res -> {
                    if (res.getNumRows() > 0) {
                        byte[] value = res.getResults().get(0).getBinary(0);

                        try {
                            return Future.succeededFuture(deserialize(value));
                        } catch (ClassNotFoundException | IOException e) {
                            return Future.failedFuture(e);
                        }
                    } else {
                        return Future.succeededFuture(null);
                    }
                });
    }

    /**
     * Check whether key exists
     *
     * @param tx  the transaction
     * @param key the cache key
     * @return true if exists, false otherwise
     */
    private Future<Boolean> exists(JDBCTransaction tx, String key) {
        return tx.query("SELECT 1 FROM " + table + " WHERE key = ? AND (expires IS NULL OR expires > NOW()) LIMIT 1", key)
                .compose(res -> Future.succeededFuture(res.getNumRows() > 0));
    }

    /**
     * Update record access time
     *
     * @param key the record key
     * @return a result future
     */
    private Future<Void> touch(String key) {
        return client.update("UPDATE " + table + " SET accessed = NOW() WHERE key = ? AND (expires IS NULL OR expires > NOW()) LIMIT 1", key).map((Void) null);
    }

    /**
     * Calculate total cache size
     *
     * @param tx the jdbc transaction
     * @return a number of records in cache
     */
    private Future<Long> size(JDBCTransaction tx) {
        return tx.query("SELECT count(*) FROM " + table).map(res -> res.getResults().get(0).getLong(0));
    }

    /**
     * Calculates actual expiration for record
     *
     * @param time the suggested expiration
     * @return an actual expiration time
     */
    private LocalDateTime expiration(@Nullable LocalDateTime time) {
        return expiresAfterWrite > 0 ? LocalDateTime.now().plus(expiresAfterWrite, ChronoUnit.MILLIS) : time;
    }

    private byte[] serialize(T t) throws IOException {
        try(ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            try(ObjectOutputStream output = new ObjectOutputStream(bytes)) {
                output.writeObject(t);
            }
            return bytes.toByteArray();
        }
    }

    private T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            try(ObjectInputStream objectInput = new ObjectInputStream(input)) {
                //noinspection unchecked
                return (T)objectInput.readObject();
            }
        }
    }
}
