package org.eightlog.thumty.loader.fs;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.loader.ContentLoader;
import org.eightlog.thumty.loader.ContentLoaderProvider;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FileSystemLoaderProvider implements ContentLoaderProvider {

    private final static String FILE_SYSTEM_LOADER_NAME = "local";

    @Override
    public String getName() {
        return FILE_SYSTEM_LOADER_NAME;
    }

    @Override
    public ContentLoader getContentLoader(Vertx vertx, JsonObject config) {
        return new FileSystemContentLoader(vertx, config);
    }
}
