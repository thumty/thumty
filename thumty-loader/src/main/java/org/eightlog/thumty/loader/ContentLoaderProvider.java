package org.eightlog.thumty.loader;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ContentLoaderProvider {

    /**
     * @return the name of content loader, must be unique
     */
    String getName();

    /**
     * Get a content loader
     *
     * @param vertx  the vertx instance
     * @param config the loader config
     * @return a content loader
     */
    ContentLoader getContentLoader(Vertx vertx, JsonObject config);
}
