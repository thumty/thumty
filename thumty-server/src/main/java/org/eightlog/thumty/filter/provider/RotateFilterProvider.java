package org.eightlog.thumty.filter.provider;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.ImageAsyncFilter;
import org.eightlog.thumty.filter.PreProcessAsyncFilterProvider;
import org.eightlog.thumty.image.filter.Rotate;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class RotateFilterProvider implements PreProcessAsyncFilterProvider {

    private static final String NAME = "rotate";

    private static final float DEFAULT_ANGLE = 0;

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new ImageAsyncFilter(vertx, new Rotate(getAngle(params)));
    }

    private float getAngle(List<String> params) {
        if (!params.isEmpty()) {
            try {
                return Float.parseFloat(params.get(0));
            }catch (NumberFormatException ignore) {
                // Ignore
            }
        }
        return DEFAULT_ANGLE;
    }
}
