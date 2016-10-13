package org.eightlog.thumty.filter.provider;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.ImageAsyncFilter;
import org.eightlog.thumty.filter.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.image.filter.RGBAdjust;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class RGBAdjustFilterProvider implements PostProcessAsyncFilterProvider {

    private final static String NAME = "rgb";

    private final static float DEFAULT_ADJUST = 0;

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new ImageAsyncFilter(vertx, new RGBAdjust(getRed(params, config), getGreen(params, config), getBlue(params, config)));
    }

    private float getRed(List<String> params, JsonObject config) {
        return getAdjust("red", 0, params, config);
    }

    private float getGreen(List<String> params, JsonObject config) {
        return getAdjust("green", 1, params, config);
    }

    private float getBlue(List<String> params, JsonObject config) {
        return getAdjust("blue", 2, params, config);
    }

    private float getAdjust(String type, int index, List<String> params, JsonObject config) {
        float amount = config.getFloat(type, DEFAULT_ADJUST);

        if (params.size() >= index + 1) {
            try {
                amount = Float.parseFloat(params.get(index));
            } catch (NumberFormatException ignore) {
                // Ignore
            }
        }

        if (Math.abs(amount) > 1) {
            amount /= 100;
        }

        if (Math.abs(amount) > 1) {
            return DEFAULT_ADJUST;
        }

        return amount;
    }

}
