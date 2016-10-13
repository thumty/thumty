package org.eightlog.thumty.filter.provider;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.ImageAsyncFilter;
import org.eightlog.thumty.filter.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.image.filter.Solarize;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class SolarizeFilterProvider implements PostProcessAsyncFilterProvider {

    private final static String NAME = "solarize";

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new ImageAsyncFilter(vertx, new Solarize());
    }

}
