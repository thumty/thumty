package org.eightlog.thumty.loader.fs;

import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.common.text.DurationParser;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FileSystemContentLoaderOptions {

    private static final String DEFAULT_BASE_PATH = "./";

    private final JsonObject config;

    public FileSystemContentLoaderOptions(JsonObject config) {
        this.config = config;
    }

    public String getBasePath() {
        return config.getString("base_path", DEFAULT_BASE_PATH);
    }

    public long getExpiresIn() {
        return DurationParser.parse(config.getString("expires_in"));
    }
}
