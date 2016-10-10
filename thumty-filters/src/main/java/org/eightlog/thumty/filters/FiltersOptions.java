package org.eightlog.thumty.filters;

import io.vertx.core.json.JsonObject;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FiltersOptions {

    private final JsonObject config;

    public FiltersOptions(JsonObject config) {
        this.config = config;
    }

    public JsonObject getFilterConfig(String filterName) {
        return config.getJsonObject(filterName, new JsonObject());
    }
}
