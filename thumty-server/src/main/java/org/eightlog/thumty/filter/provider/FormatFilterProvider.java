package org.eightlog.thumty.filter.provider;

import com.google.common.collect.ImmutableSet;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.PostProcessAsyncFilterProvider;
import org.eightlog.thumty.image.Image;

import java.util.List;
import java.util.Set;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FormatFilterProvider implements PostProcessAsyncFilterProvider {

    private final static String NAME = "format";

    private final static Set<String> SUPPORTED_FORMATS = ImmutableSet.<String>builder()
            .add("png")
            .add("jpg")
            .build();

    @Override
    public String getFilterName() {
        return NAME;
    }

    @Override
    public AsyncFilter getFilter(Vertx vertx, List<String> params, JsonObject config) {
        return new FormatFilter(getFormat(params));
    }

    private static class FormatFilter implements AsyncFilter {
        private final String format;

        FormatFilter(String format) {
            this.format = format;
        }

        @Override
        public Future<Image> apply(Image image) {
            return Future.succeededFuture(image.withFormat(format));
        }
    }

    private String getFormat(List<String> params) {
        if (!params.isEmpty()) {
            String format = params.get(0).toLowerCase();
            if (SUPPORTED_FORMATS.contains(format)) {
                return format;
            }
        }
        return "jpg";
    }
}
