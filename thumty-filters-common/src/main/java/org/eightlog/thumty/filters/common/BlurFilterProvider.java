package org.eightlog.thumty.filters.common;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filters.AsyncFilter;
import org.eightlog.thumty.filters.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.filters.sync.ImageOpAsyncFilter;
import org.eightlog.thumty.image.operations.GaussianBlur;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class BlurFilterProvider implements PostProcessAsyncFilterProvider {

    private final static String NAME = "blur";

    private final static float DEFAULT_RADIUS = 2;

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new ImageOpAsyncFilter(vertx, new GaussianBlur(getRadius(params, config)));
    }

    private float getRadius(List<String> params, JsonObject config) {
        if (!params.isEmpty()) {
            try{
                float radius = Float.parseFloat(params.get(0));
                return radius >= 0 ? radius : DEFAULT_RADIUS;
            } catch (NumberFormatException ignore) {
                // Ignore
            }
        }

        return config.getFloat("radius", DEFAULT_RADIUS);
    }
}
