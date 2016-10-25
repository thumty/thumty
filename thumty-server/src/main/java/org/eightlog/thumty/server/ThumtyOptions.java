package org.eightlog.thumty.server;

import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.server.params.ThumbParams;

import javax.annotation.Nullable;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumtyOptions {

    /**
     * Default http server host
     */
    private static final String DEFAULT_HOST = "0.0.0.0";

    /**
     * Default http server port
     */
    private static final int DEFAULT_PORT = 8080;

    private final JsonObject config;

    public ThumtyOptions(JsonObject config) {
        this.config = config;
    }

    public String getHost() {
        return config.getString("host", DEFAULT_HOST);
    }

    public Integer getPort() {
        return config.getInteger("post", DEFAULT_PORT);
    }

    public String getSecret() {
        return config.getString("secret", null);
    }

    public boolean isSecured() {
        return config.getBoolean("secured", false);
    }

    @Nullable
    public ThumbParams getVariant(String variant) {
        JsonObject params = getVariantParams(variant);
        return params != null ? ThumbParams.fromJson(params) : null;
    }

    @Nullable
    private JsonObject getVariantParams(String variant) {
        return getVariants().getJsonObject(variant);
    }

    private JsonObject getVariants() {
        return config.getJsonObject("variants", new JsonObject());
    }
}
