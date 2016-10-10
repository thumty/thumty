package org.eightlog.thumty.loader.http;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.cache.CacheManager;
import org.eightlog.thumty.cache.ContentCache;
import org.eightlog.thumty.loader.ContentLoader;
import org.eightlog.thumty.loader.ContentLoaderProvider;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class HttpContentLoaderProvider implements ContentLoaderProvider {

    private final static String HTTP_CONTENT_LOADER_NAME = "http";

    @Override
    public String getName() {
        return HTTP_CONTENT_LOADER_NAME;
    }

    @Override
    public ContentLoader getContentLoader(Vertx vertx, JsonObject config) {
        ContentCache cache = CacheManager.createShared(vertx)
                .getContentCache(HTTP_CONTENT_LOADER_NAME, getCacheConfig(config));

        return new HttpContentLoader(vertx, cache, config);
    }

    private JsonObject getCacheConfig(JsonObject config) {
        return config.getJsonObject("cache", new JsonObject());
    }
}
