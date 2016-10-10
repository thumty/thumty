package org.eightlog.thumty.loader.fs;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.loader.ContentLoader;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FileSystemContentLoader implements ContentLoader {

    private final Vertx vertx;

    private final FileSystemContentLoaderOptions options;

    public FileSystemContentLoader(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.options = new FileSystemContentLoaderOptions(config);
    }

    @Override
    public boolean canLoadResource(String location) {
        try{
            URI resource = URI.create(location);
            return "fs".equalsIgnoreCase(resource.getScheme()) || resource.getScheme() == null;
        } catch (IllegalArgumentException ignore) {
            // Ignore
        }
        return false;
    }

    @Override
    public void load(String location, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        try {
            new FileSystemLoader(vertx, Paths.get(options.getBasePath()).toRealPath(), options.getExpiresIn())
                    .handler(content -> handler.handle(Future.succeededFuture(content)))
                    .exceptionHandler(err -> handler.handle(Future.failedFuture(err)))
                    .load(location);
        }catch (IOException e) {
            handler.handle(Future.failedFuture(e));
        }
    }
}
