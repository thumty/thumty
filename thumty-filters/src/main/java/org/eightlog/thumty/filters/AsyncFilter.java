package org.eightlog.thumty.filters;

import io.vertx.core.Future;
import org.eightlog.thumty.image.io.Image;

import java.util.Arrays;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public interface AsyncFilter {

    AsyncFilter IDENTITY = Future::succeededFuture;

    /**
     * Applies filter asynchronously
     *
     * @param image the source image
     * @return a future result image
     */
    Future<Image> apply(Image image);

    /**
     * Compose async filters
     *
     * @param filters the filters
     * @return a composite async filter
     */
    default AsyncFilter compose(Iterable<AsyncFilter> filters) {
        AsyncFilter composite = null;
        for (AsyncFilter filter : filters) {
            if (composite == null) {
                composite = filter;
            } else {
                composite = composite.andThen(filter);
            }
        }
        return composite == null ? IDENTITY : composite;
    }

    /**
     * Compose async filters
     *
     * @param filters the filters
     * @return a composite async filter
     */
    default AsyncFilter compose(AsyncFilter... filters) {
        return compose(Arrays.asList(filters));
    }

    /**
     * Compose two filters
     *
     * @param after the after filter
     * @return a composite filter
     */
    default AsyncFilter andThen(AsyncFilter after) {
        return (image) -> apply(image).compose(after::apply);
    }
}
