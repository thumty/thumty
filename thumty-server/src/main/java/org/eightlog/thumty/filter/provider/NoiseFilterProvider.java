package org.eightlog.thumty.filter.provider;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.ImageAsyncFilter;
import org.eightlog.thumty.filter.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.image.filter.Noise;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class NoiseFilterProvider implements PostProcessAsyncFilterProvider {

    private final static String NAME = "noise";

    private final static float DEFAULT_AMOUNT = 0.25f;

    private final static Noise.Distribution DEFAULT_DISTRIBUTION = Noise.Distribution.UNIFORM;

    private final static float DEFAULT_DENSITY = 1;

    private final static boolean DEFAULT_MONOCHROME = false;

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new ImageAsyncFilter(vertx, new Noise(getAmount(params, config), getDistribution(params, config), getDensity(params, config), getMonochrome(params, config)));
    }

    private boolean getMonochrome(List<String> params, JsonObject config) {
        return false;
    }

    private float getDensity(List<String> params, JsonObject config) {
        float density = config.getFloat("density", DEFAULT_DENSITY);
        if (params.size() >= 3) {
            try {
                density = Float.parseFloat(params.get(2));
            } catch (NumberFormatException ignore) {
                // Ignore
            }
        }
        return density;
    }

    private Noise.Distribution getDistribution(List<String> params, JsonObject config) {

        if (params.size() >= 2) {
            if (params.get(1).equalsIgnoreCase("gaussian")) {
                return Noise.Distribution.GAUSSIAN;
            }
        }
        return DEFAULT_DISTRIBUTION;
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
