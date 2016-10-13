package org.eightlog.thumty.filter.provider;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.ImageAsyncFilter;
import org.eightlog.thumty.filter.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.image.filter.Saturation;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class SaturationFilterProvider implements PostProcessAsyncFilterProvider {

    private final static String NAME = "saturation";

    private final static float DEFAULT_AMOUNT = 0;

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new ImageAsyncFilter(vertx, new Saturation(getAmount(params, config)));
    }

    private float getAmount(List<String> params, JsonObject config) {
        float amount = config.getFloat("amount", DEFAULT_AMOUNT);

        if (!params.isEmpty()) {
            try{
                amount = Float.parseFloat(params.get(0));
            }catch (NumberFormatException ignore) {
                // Ignore
            }
        }

        if (Math.abs(amount) > 1) {
            amount /= 100;
        }

        if (Math.abs(amount) > 1) {
            return DEFAULT_AMOUNT;
        }

        return amount;
    }

}
