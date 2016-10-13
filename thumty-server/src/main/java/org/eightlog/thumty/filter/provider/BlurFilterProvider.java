package org.eightlog.thumty.filter.provider;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.ImageAsyncFilter;
import org.eightlog.thumty.filter.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.image.filter.GaussianBlur;

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
        return new ImageAsyncFilter(vertx, new GaussianBlur(getRadius(params, config)));
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
