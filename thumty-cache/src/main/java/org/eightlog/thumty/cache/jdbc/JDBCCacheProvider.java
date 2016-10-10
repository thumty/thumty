package org.eightlog.thumty.cache.jdbc;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.jdbc.JDBCClient;
import org.eightlog.thumty.cache.Cache;
import org.eightlog.thumty.cache.CacheOptions;
import org.eightlog.thumty.cache.CacheProvider;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * JDBC cache provider implementation
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCCacheProvider implements CacheProvider {

    /**
     * Vertx shared data local map name
     */
    private static final String CACHE_LOCAL_MAP_NAME = "__thumty.JDBCCacheProvider.caches";

    /**
     * Cache cleanup interval
     */
    private static final long CACHE_CLEAN_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    private final Vertx vertx;

    private final JDBCClient client;

    /**
     * Construct new cache provider
     *
     * @param vertx  the vertx instance
     * @param client the jdbc client
     */
    public JDBCCacheProvider(Vertx vertx, JDBCClient client) {
        this.vertx = vertx;
        this.client = client;
    }

    @Override
    public <T extends Serializable> Future<Cache<T>> getCache(String name, JsonObject config) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(config);

        synchronized (vertx) {
            LocalMap<String, Cache<T>> localMap = vertx.sharedData().getLocalMap(CACHE_LOCAL_MAP_NAME);
            Cache<T> cache = localMap.get(name);

            if (cache == null) {
                //noinspection unchecked
                return build(name, config).map(c -> share(name, (Cache<T>) c));
            }

            return Future.succeededFuture(cache);
        }
    }

    private <T extends Serializable> Cache<T> scheduleCleanUp(Cache<T> cache) {
        vertx.setPeriodic(CACHE_CLEAN_INTERVAL, res -> {
            cache.cleanUp();
        });
        return cache;
    }

    private <T extends Serializable> Future<Cache<T>> build(String name, JsonObject config) {
        JDBCWrapper jdbc = JDBCWrapper.create(client);

        String table = name + "_cache";

        Future<Void> createTable = jdbc.execute("CREATE TABLE IF NOT EXISTS " + table + " " +
                "(key VARCHAR(8192) NOT NULL, " +
                "value BLOB, " +
                "accessed TIMESTAMP, " +
                "expires TIMESTAMP, " +
                "PRIMARY KEY (key))");

        CacheOptions options = new CacheOptions(config);

        return createTable.map(v -> scheduleCleanUp(new JDBCCache<T>(client, table, options.getSize(), options.getExpiresAfterWrite(), options.getExpiresAfterAccess())));
    }

    private <T extends Serializable> Cache<T> share(String name, Cache<T> cache) {
        synchronized (vertx) {
            LocalMap<String, Cache<T>> localMap = vertx.sharedData().getLocalMap(CACHE_LOCAL_MAP_NAME);
            localMap.put(name, cache);
            return cache;
        }
    }
}
