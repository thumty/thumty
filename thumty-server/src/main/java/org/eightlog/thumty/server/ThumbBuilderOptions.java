package org.eightlog.thumty.server;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbBuilderOptions {

    /**
     * Thumb factory cache configuration key
     */
    private final static String CACHE_CONFIG_KEY = "cache";

    private static final float DEFAULT_QUALITY = 0.85f;

    private final JsonObject config;

    public ThumbBuilderOptions(JsonObject config) {
        Objects.requireNonNull(config);

        this.config = config;
    }

    public JsonObject getCacheConfig() {
        return config.getJsonObject(CACHE_CONFIG_KEY, new JsonObject());
    }

    public float getQuality() {
        return config.getFloat("quality", DEFAULT_QUALITY);
    }

    public boolean isSupportedFormat(String format) {
        JsonArray formats = config.getJsonArray("formats", new JsonArray());
        return formats.isEmpty() || formats.contains(format);
    }
}
