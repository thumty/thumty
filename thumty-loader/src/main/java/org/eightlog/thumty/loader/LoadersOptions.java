package org.eightlog.thumty.loader;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class LoadersOptions {

    private final JsonObject config;

    public LoadersOptions(JsonObject config) {
        Objects.requireNonNull(config);

        this.config = config;
    }

    @Nullable
    public JsonObject getLoaderConfig(String name) {
        return config.getJsonObject(name);
    }
}
