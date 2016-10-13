package org.eightlog.thumty.filter.common;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.filter.Filters;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.geometry.Direction;
import org.eightlog.thumty.server.params.*;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class AsyncThumbBuilder implements AsyncFilter {

    private final Vertx vertx;

    private final ThumbParams params;

    private final Filters filters;

    public AsyncThumbBuilder(Vertx vertx, ThumbParams params) {
        this.vertx = vertx;
        this.params = params;
        this.filters = Filters.createShared(vertx);
    }

    /**
     * Applies converters to image
     *
     * @param image the source image
     * @return a result image future
     */
    @Override
    public Future<Image> apply(Image image) {
        return AsyncFilter.compose(getFeatureFilter(), getPreProcessFilter(), getTransformFilter(), getPostProcessFilter(), new AsyncFeatureFilter(vertx)).apply(image);
    }

    private AsyncFilter getCrop() {
        ThumbCrop crop = params.getCrop();
        return crop != null ? new AsyncCrop(vertx, crop.toInsets()) : AsyncFilter.IDENTITY;
    }

    private AsyncFilter getResizer() {
        ThumbSize size = params.getSize();

        if (size != null) {
            if (params.getResize() == ThumbResize.FIT) {
                return new AsyncResizeToFit(vertx, params.getSize().toImageSize()).andThen(getFlip(size));
            } else {
                return new AsyncResizeToFill(vertx, params.getSize().toImageSize(), params.getAlign(), params.getResize()).andThen(getFlip(size));
            }
        }

        return AsyncFilter.IDENTITY;
    }

    private AsyncFilter getFlip(ThumbSize size) {
        AsyncFilter flip = AsyncFilter.IDENTITY;

        if (size.getWidth() < 0) {
            flip = new AsyncFlip(vertx, Direction.HORIZONTAL);
        }

        if (size.getHeight() < 0) {
            flip = new AsyncFlip(vertx, Direction.VERTICAL);
        }

        return flip;
    }

    private AsyncFilter getFeatureFilter() {
        return new AsyncFeaturesDetector(vertx, params.getSource(), params.getAlign(), params.getResize());
    }

    private AsyncFilter getPreProcessFilter() {
        ThumbFilters filterParams = params.getFilters();

        AsyncFilter composite = null;

        if (filterParams != null) {
            for (ThumbFilter params : filterParams) {
                AsyncFilter filter = filters.getPreProcessFilter(params.getName(), params.getArguments());

                if (composite == null) {
                    composite = filter;
                } else {
                    composite = composite.andThen(filter);
                }
            }
        }

        return composite != null ? composite : IDENTITY;
    }

    private AsyncFilter getPostProcessFilter() {
        ThumbFilters filterParams = params.getFilters();

        AsyncFilter composite = null;

        if (filterParams != null) {
            for (ThumbFilter params : filterParams) {
                AsyncFilter filter = filters.getPostProcessFilter(params.getName(), params.getArguments());

                if (composite == null) {
                    composite = filter;
                } else {
                    composite = composite.andThen(filter);
                }
            }
        }

        return composite != null ? composite : IDENTITY;
    }

    private AsyncFilter getTransformFilter() {
        return AsyncFilter.compose(getCrop(), getResizer());
    }
}
