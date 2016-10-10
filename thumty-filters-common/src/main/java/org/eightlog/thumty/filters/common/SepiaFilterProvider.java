package org.eightlog.thumty.filters.common;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filters.AsyncFilter;
import org.eightlog.thumty.filters.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.filters.sync.ImageOpAsyncFilter;
import org.eightlog.thumty.image.operations.Sepia;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class SepiaFilterProvider implements PostProcessAsyncFilterProvider {

    private final static String NAME = "sepia";

    private final static int DEFAULT_INTENSITY = 0;

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new ImageOpAsyncFilter(vertx, new Sepia(getIntensity(params, config)));
    }

    private int getIntensity(List<String> params, JsonObject config) {
        if (!params.isEmpty()) {
            try{
                return Integer.parseInt(params.get(0));
            }catch (NumberFormatException ignore) {
                // Ignore
            }
        }
        return config.getInteger("intensity", DEFAULT_INTENSITY);
    }

}
