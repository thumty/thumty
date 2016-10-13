package org.eightlog.thumty.filter;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public interface AsyncFilterProvider {

    /**
     * Get the filter name
     *
     * @return a filter name
     */
    String getFilterName();

    /**
     * Get filter instance
     *
     * @param vertx  the vertx instance
     * @param params the filter params
     * @param config the filter configuration
     * @return a filter instance
     */
    AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config);
}
