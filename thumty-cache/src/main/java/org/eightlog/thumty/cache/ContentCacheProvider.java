package org.eightlog.thumty.cache;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ContentCacheProvider {

    /**
     * Get or create {@link ContentCache}
     *
     * @param name   the content cache name
     * @param config the cache configuration
     * @return a cache future
     */
    Future<ContentCache> getContentCache(String name, JsonObject config);
}
