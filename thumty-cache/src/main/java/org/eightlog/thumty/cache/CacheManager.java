package org.eightlog.thumty.cache;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider;
import org.eightlog.thumty.cache.jdbc.JDBCCacheProvider;
import org.eightlog.thumty.cache.jdbc.JDBCContentCacheProvider;
import org.eightlog.thumty.cache.utils.ForwardCache;
import org.eightlog.thumty.cache.utils.ForwardContentCache;

import java.io.Serializable;
import java.util.Objects;

/**
 * Cache manager.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class CacheManager implements Shareable {

    private final static Logger log = LoggerFactory.getLogger(CacheManager.class);

    /**
     * Default cache manager name
     */
    private final static String DEFAULT_CACHE_MANAGER_NAME   = "__thumty.CacheManager.default";

    /**
     * Default cache manager shared data lookup name
     */
    private final static String CACHE_MANAGER_LOCAL_MAP_NAME = "__thumty.CacheManager";

    /**
     * Vertx configuration key for cache manager
     */
    private final static String CACHE_CONFIGURATION_KEY = "cache";

    private final ContentCacheProvider contentCacheProvider;

    private final CacheProvider cacheProvider;

    private final CacheManagerOptions options;

    /**
     * Cache manager constructor
     *
     * @param vertx  the vertx instance
     * @param config the configuration
     */
    public CacheManager(Vertx vertx, JsonObject config) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(config);

        this.options = new CacheManagerOptions(config);

        log.debug("Creating new \"{0}\" cache manager", options.getType());

        if ("local".equalsIgnoreCase(options.getType())) {
            JDBCClient client = getJDBCClient(vertx);

            this.contentCacheProvider = new JDBCContentCacheProvider(vertx, client, options.getPath());
            this.cacheProvider = new JDBCCacheProvider(vertx, client);
        } else {
            throw new IllegalStateException("Unsupported cache type " + options.getType());
        }
    }

    /**
     * Get cache manager configuration from vertx instance
     *
     * @param vertx the vertx instance
     * @return a configuration
     */
    private static JsonObject getCacheConfig(Vertx vertx) {
        Context context = vertx.getOrCreateContext();
        JsonObject config = context.config() != null ? context.config() : new JsonObject();
        return config.getJsonObject(CACHE_CONFIGURATION_KEY, new JsonObject());
    }

    /**
     * Create shared cache manager with default name and configuration from vertx
     *
     * @param vertx the vertx instance
     * @return a cache manager
     */
    public static CacheManager createShared(Vertx vertx) {
        return createShared(vertx, getCacheConfig(vertx), DEFAULT_CACHE_MANAGER_NAME);
    }

    /**
     * Create shared cache manager with default name
     *
     * @param vertx  the vertx instance
     * @param config the cache manager configuration
     * @return a cache manager
     */
    public static CacheManager createShared(Vertx vertx, JsonObject config) {
        return createShared(vertx, config, DEFAULT_CACHE_MANAGER_NAME);
    }

    /**
     * Create shared cache manager
     *
     * @param vertx  the vertx instance
     * @param config the cache manager configuration
     * @param name   the cache manager name
     * @return a cache manager
     */
    public synchronized static CacheManager createShared(Vertx vertx, JsonObject config, String name) {
        LocalMap<String, CacheManager> localMap = vertx.sharedData().getLocalMap(CACHE_MANAGER_LOCAL_MAP_NAME);

        CacheManager loaders = localMap.get(name);

        if (loaders == null) {
            localMap.put(name, loaders = new CacheManager(vertx, config));
        }

        return loaders;
    }

    /**
     * Get {@link ContentCache} instance
     *
     * @param name   the cache name
     * @param config the cache configuration
     * @return a content cache
     */
    public ContentCache getContentCache(String name, JsonObject config) {
        return new ForwardContentCache(contentCacheProvider.getContentCache(name, config));
    }

    /**
     * Get {@link Cache} instance
     *
     * @param name   the cache name
     * @param config the cache configuration
     * @param <T>    the type of cache value
     * @return a cache
     */
    public <T extends Serializable> Cache<T> getCache(String name, JsonObject config) {
        return new ForwardCache<>(cacheProvider.getCache(name, config));
    }

    private JDBCClient getJDBCClient(Vertx vertx) {
        return JDBCClient.createShared(vertx, new JsonObject()
                .put("provider_class",    HikariCPDataSourceProvider.class.getName())
                .put("jdbcUrl",           options.getDatasourceUrl())
                .put("driverClassName",   options.getDatasourceDriver())
                .put("connectionTimeout", options.getDatasourceConnectionTimeout())
                .put("maximumPoolSize",   options.getDatasourceMaximumPoolSize())
                .put("minimumIdle",       options.getDatasourceMinimumPoolSize()));
    }

}
