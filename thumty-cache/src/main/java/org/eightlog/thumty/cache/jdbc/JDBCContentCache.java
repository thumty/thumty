package org.eightlog.thumty.cache.jdbc;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.jdbc.JDBCClient;
import org.eightlog.thumty.cache.ContentCache;
import org.eightlog.thumty.cache.content.BinaryExpirableAttributedContent;
import org.eightlog.thumty.common.jdbc.JDBCTransaction;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.eightlog.thumty.store.ExpirableAttributedContent;
import org.eightlog.thumty.store.content.Content;
import org.eightlog.thumty.store.content.ContentStore;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCContentCache implements ContentCache {

    private final static int DEFAULT_BATCH_SIZE = 1000;

    private final static long DEFAULT_CACHE_SIZE = 0;

    private final JDBCWrapper client;

    private final ContentStore contentStore;

    private final String table;

    private final long cacheSize;

    private final int batchSize;

    private final long expiresAfterAccess;

    private final long expiresAfterWrite;

    /**
     * Construct new content cache
     *
     * @param contentStore       the content store
     * @param client             the jdbc client
     * @param table              the db table name
     * @param cacheSize          the cache size in bytes, 0 if not limited
     * @param expiresAfterAccess the time in ms
     * @param expiresAfterWrite  the time in ms
     * @param batchSize          the cleanup batch size
     */
    public JDBCContentCache(ContentStore contentStore, JDBCClient client, String table,
                            long cacheSize, long expiresAfterAccess, long expiresAfterWrite, int batchSize) {
        Objects.requireNonNull(contentStore, "contentStore");
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(table, "table");

        this.client = JDBCWrapper.create(client);
        this.contentStore = contentStore;
        this.table = table;

        this.cacheSize = cacheSize;
        this.batchSize = batchSize;

        this.expiresAfterAccess = expiresAfterAccess;
        this.expiresAfterWrite = expiresAfterWrite;
    }

    public JDBCContentCache(ContentStore contentStore, JDBCClient client, String table,
                            long cacheSize, long expiresAfterAccess, long expiresAfterWrite) {
        this(contentStore, client, table, cacheSize, expiresAfterAccess, expiresAfterWrite, DEFAULT_BATCH_SIZE);
    }

    public JDBCContentCache(ContentStore contentStore, JDBCClient client, String table) {
        this(contentStore, client, table, DEFAULT_CACHE_SIZE, 0, 0, DEFAULT_BATCH_SIZE);
    }

    @Override
    public void getIfPresent(String key, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        // Update access time when possible
        touch(key);

        exists(key)
                .compose(exists -> {
                    if (exists) {
                        return expires(key).compose(expires -> contentStore.read(key).map(content -> record(expires, content)));
                    } else {
                        return Future.succeededFuture(null);
                    }
                })
                .setHandler(handler);
    }

    @Override
    public void put(String key, ReadStream<Buffer> content, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        put(key, content, expiration(null), handler);
    }

    @Override
    public void put(String key, ReadStream<Buffer> content, LocalDateTime expires, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        content.pause();

        LocalDateTime exp = expiration(expires);

        contentStore.write(key, content).compose(file ->
                client.begin(tx ->
                        exists(tx, key)
                                .compose(exists -> {
                                    if (exists) {
                                        return update(tx, key, file, exp);
                                    } else {
                                        return insert(tx, key, file, exp);
                                    }
                                })
                                .compose(tx::commit)
                                .map(v -> record(exp, file))

                )
        ).setHandler(handler);
    }

    @Override
    public void put(String source, String target, LocalDateTime expires, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        // Update access time when possible
        touch(source);

        LocalDateTime exp = expiration(expires);

        client.begin(tx ->
                exists(tx, source)
                        .compose(v -> delete(tx, target))
                        .compose(v -> contentStore.copy(source, target).compose(file -> insert(tx, target, file, exp)))
                        .compose(tx::commit)
                        .map(file -> record(exp, file))
        ).setHandler(handler);
    }

    @Override
    public void invalidate(String key, Handler<AsyncResult<Void>> handler) {
        client.begin(tx ->
                delete(tx, key)
                        .compose(exists -> {
                            if (exists) {
                                return contentStore.delete(key);
                            } else {
                                return Future.succeededFuture();
                            }
                        })
                        .compose(tx::commit)
        ).setHandler(handler);
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
        CompositeFuture.join(
                expiredEntries().compose(this::invalidateAll),             // Invalidate expired entries
                entriesExpiredByAccessTime().compose(this::invalidateAll), // Invalidate entries by access time
                entriesExceedingSize().compose(this::invalidateAll)        // Invalidate entries which exceeds size
        ).map((Void) null).setHandler(handler);
    }

    private ExpirableAttributedContent record(LocalDateTime expires, Content content) {
        return new BinaryExpirableAttributedContent(expires, content);
    }

    private Future<Content> insert(JDBCTransaction tx, String key, Content content, LocalDateTime expires) {
        Date expireDate = expires != null ? Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()) : null;
        long size = content.getAttributes().getSize();

        return tx.update("INSERT INTO " + table + " (key, size, accessed, expires) VALUES (?, ?, NOW(), ?)", key, size, expireDate).map(content);
    }

    private Future<Content> update(JDBCTransaction tx, String key, Content content, LocalDateTime expires) {
        Date expireDate = expires != null ? Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()) : null;
        long size = content.getAttributes().getSize();

        return tx.update("UPDATE " + table + " SET accessed = NOW(), expires = ?, size = ? WHERE key = ?", expireDate, size, key).map(content);
    }

    private Future<Boolean> delete(JDBCTransaction tx, String key) {
        return tx.update("DELETE FROM " + table + " WHERE key = ?", key).compose(res -> Future.succeededFuture(res.getUpdated() > 0));
    }

    private Future<Boolean> exists(String key) {
        return client.begin(tx -> exists(tx, key));
    }

    private Future<Boolean> exists(JDBCTransaction tx, String key) {
        return tx.query("SELECT 1 FROM " + table + " WHERE key = ? AND (expires IS NULL OR expires > NOW()) LIMIT 1", key)
                .compose(res -> Future.succeededFuture(res.getNumRows() > 0));
    }

    private Future<Void> touch(String key) {
        return client.update("UPDATE " + table + " SET accessed = NOW() WHERE key = ? LIMIT 1", key).map((Void) null);
    }

    private Future<LocalDateTime> expires(String key) {
        return client.begin(tx -> expires(tx, key));
    }

    private Future<LocalDateTime> expires(JDBCTransaction tx, String key) {
        return tx.query("SELECT expires FROM " + table + " WHERE key = ? LIMIT 1", key).map(rows -> {
            if (rows.getNumRows() > 0 && rows.getResults().get(0).getInstant(0) != null) {
                return LocalDateTime.ofInstant(rows.getResults().get(0).getInstant(0), ZoneId.systemDefault());
            }
            return null;
        });
    }

    /**
     * Calculate total content size
     *
     * @param tx the jdbc transaction
     * @return a total size of content stored
     */
    private Future<Long> size(JDBCTransaction tx) {
        return tx.query("SELECT SUM(size) FROM " + table).map(res -> res.getResults().get(0).getLong(0));
    }

    /**
     * @return a list of entries keys which are expired
     */
    private Future<List<String>> expiredEntries() {
        return client.begin(tx ->
                tx.query("SELECT key FROM " + table + " WHERE expires < NOW() LIMIT " + batchSize)
                        .map(res -> res.getResults().stream().map(e -> e.getString(0)).collect(Collectors.toList()))
        );
    }

    /**
     * @return list of entries keys which sum size exceeds total size
     */
    private Future<List<String>> entriesExceedingSize() {
        if (cacheSize > 0) {
            return client.begin(tx ->
                    size(tx).compose(totalSize -> {
                        if (totalSize != null && totalSize > cacheSize) {
                            return tx.query("SELECT key, size FROM " + table + " ORDER BY accessed ASC, size DESC LIMIT " + batchSize)
                                    .map(res -> {
                                        long diff = totalSize - cacheSize;

                                        List<String> keys = new ArrayList<>();

                                        for (int i = 0; i < res.getNumRows() && diff > 0; i++) {
                                            String key = res.getResults().get(i).getString(0);
                                            long size = res.getResults().get(i).getLong(1);

                                            keys.add(key);
                                            diff -= size;
                                        }

                                        return keys;
                                    });
                        } else {
                            return Future.succeededFuture(Collections.emptyList());
                        }
                    })
            );
        } else {
            return Future.succeededFuture(Collections.emptyList());
        }
    }

    /**
     * @return a list of keys that corresponds to entries which where accessed earlier than {@code expiresAfterAccess} ms
     */
    private Future<List<String>> entriesExpiredByAccessTime() {
        if (expiresAfterAccess > 0) {
            Date timestamp = Date.from(
                    LocalDateTime.now()
                            .minus(expiresAfterAccess, ChronoUnit.MILLIS)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );

            return client.begin(tx ->
                    tx.query("SELECT key FROM " + table + " WHERE accessed < ? ORDER BY accessed ASC LIMIT " + batchSize, timestamp)
                            .map(res -> res.getResults().stream().map(r -> r.getString(0)).collect(Collectors.toList()))
            );
        } else {
            return Future.succeededFuture(Collections.emptyList());
        }
    }

    private LocalDateTime expiration(@Nullable LocalDateTime time) {
        return expiresAfterWrite > 0 ? LocalDateTime.now().plus(expiresAfterWrite, ChronoUnit.MILLIS) : time;
    }
}
