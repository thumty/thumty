package org.eightlog.thumty.loader;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Loaders implements Shareable {

    private final static Logger log = LoggerFactory.getLogger(Loaders.class);

    private static final String LOADERS_LOCAL_MAP_NAME = "__thumty.Loaders";
    private static final String DEFAULT_LOADERS_NAME = "__thumty.Loaders.default";

    private final static String LOADER_CONFIG_KEY = "loaders";

    private final Vertx vertx;

    private final LoadersOptions options;

    private List<ContentLoader> loaders;

    public static Loaders createShared(Vertx vertx) {
        return createShared(vertx, getLoaderConfig(vertx), DEFAULT_LOADERS_NAME);
    }

    public static Loaders createShared(Vertx vertx, JsonObject config) {
        return createShared(vertx, config, DEFAULT_LOADERS_NAME);
    }

    public synchronized static Loaders createShared(Vertx vertx, JsonObject config, String name) {
        LocalMap<String, Loaders> localMap = vertx.sharedData().getLocalMap(LOADERS_LOCAL_MAP_NAME);

        Loaders loaders = localMap.get(name);

        if (loaders == null) {
            localMap.put(name, loaders = new Loaders(vertx, config));
        }

        return loaders;
    }

    private static JsonObject getLoaderConfig(Vertx vertx) {
        Context context = vertx.getOrCreateContext();
        JsonObject config = context.config() != null ? context.config() : new JsonObject();
        return config.getJsonObject(LOADER_CONFIG_KEY, new JsonObject());
    }

    public Loaders(Vertx vertx, JsonObject config) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(config);

        this.vertx = vertx;
        this.options = new LoadersOptions(config);
    }

    public ContentLoader getLoader(String resource) {
        for (ContentLoader loader : getContentLoaders()) {
            if (loader.canLoadResource(resource)) {
                return loader;
            }
        }

        return new FaultyContentLoader(new LoaderException("No loader for resource: " + resource));
    }

    private List<ContentLoader> getContentLoaders() {
        synchronized (vertx) {
            if (loaders == null) {
                log.debug("Initializing loaders");

                loaders = new ArrayList<>();

                ServiceLoader<ContentLoaderProvider> serviceLoader = ServiceLoader.load(ContentLoaderProvider.class);

                for (ContentLoaderProvider provider : serviceLoader) {
                    JsonObject config = options.getLoaderConfig(provider.getName());

                    if (config != null) {
                        log.debug("Registered loader \"{0}\"", provider.getName());

                        loaders.add(provider.getContentLoader(vertx, config));
                    } else {
                        log.debug("No configuration for \"{0}\", skipping", provider.getName());
                    }
                }
            }

            return loaders;
        }
    }
}
