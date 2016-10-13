package org.eightlog.thumty.filter;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Filters implements Shareable {

    private final static Logger log = LoggerFactory.getLogger(Filters.class);

    private static final String FILTERS_LOCAL_MAP_NAME = "__thumty.Filters";
    private static final String DEFAULT_FILTERS_NAME = "__thumty.Filters.default";

    private final static String FILTER_CONFIG_KEY = "filters";

    private final Vertx vertx;

    private final FiltersOptions options;

    private Map<String, AsyncFilterProvider> providers;

    private Map<String, AsyncFilterProvider> preProcessFilters;

    private Map<String, AsyncFilterProvider> postProcessFilters;

    public Filters(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.options = new FiltersOptions(config);
    }

    public static Filters createShared(Vertx vertx) {
        return createShared(vertx, getFiltersConfig(vertx), DEFAULT_FILTERS_NAME);
    }

    public static Filters createShared(Vertx vertx, JsonObject config) {
        return createShared(vertx, config, DEFAULT_FILTERS_NAME);
    }

    public synchronized static Filters createShared(Vertx vertx, JsonObject config, String name) {
        LocalMap<String, Filters> localMap = vertx.sharedData().getLocalMap(FILTERS_LOCAL_MAP_NAME);

        Filters filters = localMap.get(name);

        if (filters == null) {
            localMap.put(name, filters = new Filters(vertx, config));
        }

        return filters;
    }

    private static JsonObject getFiltersConfig(Vertx vertx) {
        Context context = vertx.getOrCreateContext();
        JsonObject config = context.config() != null ? context.config() : new JsonObject();
        return config.getJsonObject(FILTER_CONFIG_KEY, new JsonObject());
    }

    public AsyncFilter getPreProcessFilter(String name, List<String> params) {
        AsyncFilterProvider provider = getPreProcessFilters().get(name);

        if (provider != null) {
            return provider.getFilter(vertx, params, options.getFilterConfig(name));
        } else {
            return AsyncFilter.IDENTITY;
        }
    }

    public AsyncFilter getPostProcessFilter(String name, List<String> params) {
        AsyncFilterProvider provider = getPostProcessFilters().get(name);

        if (provider != null) {
            return provider.getFilter(vertx, params, options.getFilterConfig(name));
        } else {
            return AsyncFilter.IDENTITY;
        }
    }

    private synchronized Map<String, AsyncFilterProvider> getPreProcessFilters() {
        if (preProcessFilters == null) {
            log.debug("Initializing preprocess filters");

            preProcessFilters = new HashMap<>();

            ServiceLoader<PreProcessAsyncFilterProvider> registry = ServiceLoader.load(PreProcessAsyncFilterProvider.class);

            for (AsyncFilterProvider provider : registry) {
                log.debug("Added preprocess filter \"{0}\"", provider.getFilterName());
                preProcessFilters.put(provider.getFilterName(), provider);
            }
        }

        return preProcessFilters;
    }

    private synchronized Map<String, AsyncFilterProvider> getPostProcessFilters() {
        if (postProcessFilters == null) {
            log.debug("Initializing postprocess filters");

            postProcessFilters = new HashMap<>();

            ServiceLoader<PostProcessAsyncFilterProvider> registry = ServiceLoader.load(PostProcessAsyncFilterProvider.class);

            for (AsyncFilterProvider provider : registry) {
                log.debug("Added postprocess filter \"{0}\"", provider.getFilterName());
                postProcessFilters.put(provider.getFilterName(), provider);
            }
        }

        return postProcessFilters;
    }

}
