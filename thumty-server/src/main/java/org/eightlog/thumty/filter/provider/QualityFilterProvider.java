package org.eightlog.thumty.filter.provider;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.image.Image;

import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class QualityFilterProvider implements PostProcessAsyncFilterProvider {

    private final static String NAME = "quality";

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new QualityFilter(getQuality(params));
    }

    private static class QualityFilter implements AsyncFilter {
        private final float quality;

        QualityFilter(float quality) {
            this.quality = quality;
        }

        @Override
        public Future<Image> apply(Image image) {
            return Future.succeededFuture(image.withQuality(quality));
        }
    }

    private float getQuality(List<String> params) {
        if (!params.isEmpty()) {
            try {
                float quality = Float.parseFloat(params.get(0));

                if (quality > 1 && quality <= 100) {
                    return quality / 100;
                }

                if (quality > 0 && quality <= 1) {
                    return quality;
                }
            } catch (NumberFormatException ignore) {
                // Ignore
            }
        }

        return 1;
    }
}
