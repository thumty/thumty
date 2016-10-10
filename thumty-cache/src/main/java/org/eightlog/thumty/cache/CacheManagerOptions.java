package org.eightlog.thumty.cache;

import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class CacheManagerOptions {

    private static final String DEFAULT_TYPE = "local";
    private static final String DEFAULT_PATH = "./";

    private final JsonObject config;

    public CacheManagerOptions(JsonObject config) {
        Objects.requireNonNull(config);

        this.config = config;
    }

    public String getType() {
        return config.getString("type", DEFAULT_TYPE);
    }

    public String getPath() {
        return config.getString("path", DEFAULT_PATH);
    }

    public String getDatasourceUrl() {
        return getDatasourceConfig().getString("jdbc_url", "jdbc:h2:" + getPath() + "/db;LOCK_TIMEOUT=100;MVCC=true");
    }

    public String getDatasourceDriver() {
        return getDatasourceConfig().getString("driver_class", "org.h2.Driver");
    }

    public long getDatasourceConnectionTimeout() {
        return getDatasourceConfig().getLong("connection_timeout", 1000L);
    }

    public int getDatasourceMaximumPoolSize() {
        return getDatasourceConfig().getInteger("max_pool_size", 30);
    }

    public int getDatasourceMinimumPoolSize() {
        return getDatasourceConfig().getInteger("min_pool_size", 3);
    }

    private JsonObject getDatasourceConfig() {
        return config.getJsonObject("datasource", new JsonObject());
    }
}
