package org.eightlog.thumty.cache;

import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.common.text.ByteSizeParser;
import org.eightlog.thumty.common.text.DurationParser;

import java.util.Objects;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class CacheOptions {

    private final JsonObject config;

    public CacheOptions(JsonObject config) {
        Objects.requireNonNull(config);

        this.config = config;
    }

    public long getSize() {
        return ByteSizeParser.parse(config.getString("size"));
    }

    public long getExpiresAfterAccess() {
        return DurationParser.parse(config.getString("expires_after_access"));
    }

    public long getExpiresAfterWrite() {
        return DurationParser.parse(config.getString("expires_after_write"));
    }
}
