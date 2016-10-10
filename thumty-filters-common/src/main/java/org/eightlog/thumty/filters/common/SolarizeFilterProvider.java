package org.eightlog.thumty.filters.common;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filters.AsyncFilter;
import org.eightlog.thumty.filters.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.filters.sync.ImageOpAsyncFilter;
import org.eightlog.thumty.image.operations.Solarize;

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
        return new ImageOpAsyncFilter(vertx, new Solarize());
    }

}
