package org.eightlog.thumty.cache;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;

/**
 * A cache provider.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface CacheProvider {
    /**
     * Get {@link Cache} by name
     *
     * @param name   the cache name
     * @param config the configuration
     * @param <T>    the type of cache value
     * @return a cache
     */
    <T extends Serializable> Future<Cache<T>> getCache(String name, JsonObject config);
}
