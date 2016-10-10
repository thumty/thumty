package org.eightlog.thumty.cache.jdbc;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.jdbc.JDBCClient;
import org.eightlog.thumty.cache.CacheOptions;
import org.eightlog.thumty.cache.ContentCache;
import org.eightlog.thumty.cache.ContentCacheProvider;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.eightlog.thumty.store.content.jdbc.JDBCContentStoreProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * JDBC content cache provider implementation
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCContentCacheProvider implements ContentCacheProvider {

    /**
     * Vertx shared data local map name
     */
    private static final String CACHE_LOCAL_MAP_NAME = "__thumty.JDBCContentCacheProvider.caches";

    /**
     * Cache cleanup interval
     */
    private static final long CACHE_CLEAN_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    private final Vertx vertx;

    private final JDBCContentStoreProvider contentStoreProvider;

    private final JDBCClient client;

    /**
     * Cache provider constructor
     *
     * @param vertx    the vertx instance
     * @param client   the jdbc client
     * @param basePath the content store base path
     */
    public JDBCContentCacheProvider(Vertx vertx, JDBCClient client, String basePath) {
        this.vertx = vertx;
        this.client = client;
        this.contentStoreProvider = new JDBCContentStoreProvider(vertx, client, basePath);
    }

    @Override
    public Future<ContentCache> getContentCache(String name, JsonObject config) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(config);

        synchronized (vertx) {
            LocalMap<String, ContentCache> localMap = vertx.sharedData().getLocalMap(CACHE_LOCAL_MAP_NAME);
            ContentCache cache = localMap.get(name);

            if (cache == null) {
                return build(name, config).map(c -> share(name, c));
            }

            return Future.succeededFuture(cache);
        }
    }

    /**
     * Schedule clean up on given cache
     *
     * @param cache the cache instance
     * @return a cache instance
     */
    private ContentCache scheduleCleanUp(ContentCache cache) {
        vertx.setPeriodic(CACHE_CLEAN_INTERVAL, res -> {
            cache.cleanUp();
        });
        return cache;
    }

    private Future<ContentCache> build(String name, JsonObject config) {
        JDBCWrapper jdbc = JDBCWrapper.create(client);

        String table = name + "_content_cache";

        Future<Void> createTable = jdbc.execute("CREATE TABLE IF NOT EXISTS " + table + " " +
                "(key VARCHAR(8192) NOT NULL, " +
                "size BIGINT, " +
                "accessed TIMESTAMP, " +
                "expires TIMESTAMP, " +
                "PRIMARY KEY (key))");

        CacheOptions options = new CacheOptions(config);

        return createTable
                .compose(v -> contentStoreProvider.create(name, config))
                .map(store -> scheduleCleanUp(new JDBCContentCache(store, client, table, options.getSize(), options.getExpiresAfterWrite(), options.getExpiresAfterAccess())));
    }


    /**
     * Shares the cache
     *
     * @param name  the cache name
     * @param cache the cache instance
     * @return a cache instance
     */
    private ContentCache share(String name, ContentCache cache) {
        synchronized (vertx) {
            LocalMap<String, ContentCache> localMap = vertx.sharedData().getLocalMap(CACHE_LOCAL_MAP_NAME);
            localMap.put(name, cache);
            return cache;
        }
    }
}
