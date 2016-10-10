package org.eightlog.thumty.server;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.eightlog.thumty.feature.FeatureAlign;
import org.eightlog.thumty.feature.FeatureDetectionService;
import org.eightlog.thumty.feature.Features;
import org.eightlog.thumty.filters.AsyncFilter;
import org.eightlog.thumty.filters.Filters;
import org.eightlog.thumty.image.geometry.Align;
import org.eightlog.thumty.image.geometry.FixedAlign;
import org.eightlog.thumty.image.geometry.FocusAlign;
import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteCoordinate;
import org.eightlog.thumty.image.io.Image;
import org.eightlog.thumty.image.operations.*;
import org.eightlog.thumty.server.params.*;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ParametrizedThumbBuilder {

    private final Vertx vertx;

    private final ThumbParams params;

    private final FeatureDetectionService detection;

    private final Filters filters;

    public ParametrizedThumbBuilder(Vertx vertx, ThumbParams params) {
        this.vertx = vertx;
        this.params = params;
        this.filters = Filters.createShared(vertx);
        this.detection = FeatureDetectionService.createProxy(vertx, FeatureDetectionService.FEATURE_DETECTOR_ADDRESS);
    }

    /**
     * Applies converters to image
     *
     * @param image the source image
     * @return a result image future
     */
    public Future<Image> apply(Image image) {
        return preProcess(image).compose(img ->
                getTransform(img).compose(transform -> apply(img, transform)).compose(this::postProcess)
        );
    }

    private Future<Image> apply(Image image, ImageOp transform) {
        Future<Image> future = Future.future();

        vertx.executeBlocking(block -> {
            try {
                block.complete(transform.apply(image));
            } catch (Throwable e) {
                block.fail(e);
            }
        }, false, future.completer());

        return future;
    }

    private Future<Image> preProcess(Image image) {
        ThumbFilters filterParams = params.getFilters();

        AsyncFilter composite = null;

        if (filterParams != null) {
            for (Map.Entry<String, ThumbFilter> entry : filterParams.getFilters().entrySet()) {
                AsyncFilter filter = filters.getPreProcessFilter(entry.getKey(), entry.getValue().getArguments());

                if (composite == null) {
                    composite = filter;
                } else {
                    composite = composite.andThen(filter);
                }
            }
        }

        return composite != null ? composite.apply(image) : Future.succeededFuture(image);
    }

    private Future<Image> postProcess(Image image) {
        ThumbFilters filterParams = params.getFilters();

        AsyncFilter composite = null;

        if (filterParams != null) {
            for (Map.Entry<String, ThumbFilter> entry : filterParams.getFilters().entrySet()) {
                AsyncFilter filter = filters.getPostProcessFilter(entry.getKey(), entry.getValue().getArguments());

                if (composite == null) {
                    composite = filter;
                } else {
                    composite = composite.andThen(filter);
                }
            }
        }

        return composite != null ? composite.apply(image) : Future.succeededFuture(image);
    }

    private Future<ImageOp> getTransform(Image image) {
        return getAlign(image).map(align -> ImageOp.compose(getCrop(), getResizer(align)));
    }

    private ImageOp getCrop() {
        ThumbCrop crop = params.getCrop();
        return crop != null ? new Crop(crop.toInsets()) : ImageOp.IDENTITY;
    }

    private ImageOp getResizer(Align align) {
        ThumbSize size = params.getSize();

        if (size != null) {
            if (params.getResize() == ThumbResize.FIT) {
                return new ResizeToFit(params.getSize().toImageSize()).andThen(getFlip(size));
            } else {
                return new ResizeToFill(params.getSize().toImageSize(), align).andThen(getFlip(size));
            }
        }

        return ImageOp.IDENTITY;
    }

    private ImageOp getFlip(ThumbSize size) {
        ImageOp flip = ImageOp.IDENTITY;

        if (size.getWidth() < 0) {
            flip = Flip.HORIZONTAL;
        }

        if (size.getHeight() < 0) {
            flip = flip.andThen(Flip.VERTICAL);
        }

        return flip;
    }

    private Future<Align> getAlign(Image image) {
        ThumbAlign align = params.getAlign();
        ThumbResize resize = params.getResize() != null ? params.getResize() : ThumbResize.FILL;

        if (align != null && resize == ThumbResize.FILL) {
            if (align.getType() == ThumbAlignType.FACE) {
                return getFaceAlign(image, align.getNumber());
            }

            if (align.getType() == ThumbAlignType.AUTO) {
                return getAutoAlign(image);
            }

            if (align.getType() == ThumbAlignType.FOCUS) {
                return Future.succeededFuture(new FocusAlign(new RelativeOrAbsoluteCoordinate(align.getX(), align.getY())));
            }

            switch (align.getType()) {
                case BOTTOM:
                    return Future.succeededFuture(FixedAlign.BOTTOM);
                case TOP:
                    return Future.succeededFuture(FixedAlign.TOP);
                case LEFT:
                    return Future.succeededFuture(FixedAlign.LEFT);
                case RIGHT:
                    return Future.succeededFuture(FixedAlign.RIGHT);
            }
        }

        return Future.succeededFuture(FixedAlign.CENTER);
    }

    private Future<Align> getFaceAlign(Image image, int num) {
        Future<Align> future = Future.future();

        detection.detectFace(params.getSource(), res -> {
            if (res.succeeded()) {
                Features features = res.result();
                future.complete(featureAlign(image, features.nthRegion(num < 1 ? 0 : num - 1)));
            } else {
                future.fail(res.cause());
            }
        });

        return future;
    }

    private Future<Align> getAutoAlign(Image image) {
        Future<Align> future = Future.future();

        detection.detectAny(params.getSource(), res -> {
            if (res.succeeded()) {
                Features features = res.result();
                future.complete(featureAlign(image, features));
            } else {
                future.fail(res.cause());
            }
        });

        return future;
    }

    private Align featureAlign(Image image, Features features) {
        if (!features.isEmpty()) {
            ThumbCrop crop = params.getCrop();
            if (crop != null) {
                BufferedImage bufferedImage = image.getBufferedImage();
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                return new FeatureAlign(features.crop(crop.toInsets().calculate(width, height)));
            }
            return new FeatureAlign(features);
        }

        return FixedAlign.CENTER;
    }
}
