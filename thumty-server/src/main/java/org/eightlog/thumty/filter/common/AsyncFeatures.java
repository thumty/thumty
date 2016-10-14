package org.eightlog.thumty.filter.common;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.eightlog.thumty.feature.DetectionTarget;
import org.eightlog.thumty.feature.FeatureDetectionService;
import org.eightlog.thumty.feature.Features;
import org.eightlog.thumty.filter.AsyncFilter;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.server.params.ThumbAlign;
import org.eightlog.thumty.server.params.ThumbResize;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class AsyncFeatures implements AsyncFilter {

    private final FeatureDetectionService detector;
    private final String resource;
    private final ThumbAlign align;
    private final ThumbResize resize;

    public AsyncFeatures(Vertx vertx, String resource, ThumbAlign align, ThumbResize resize) {
        this.detector = FeatureDetectionService.createProxy(vertx, FeatureDetectionService.FEATURE_DETECTOR_ADDRESS);
        this.resource = resource;
        this.align = align;
        this.resize = resize;
    }

    @Override
    public Future<Image> apply(Image image) {
        Future<Features> future = Future.future();

        if (align != null && (resize == null || resize == ThumbResize.FILL)) {
            switch (align.getType()) {
                case FACE:
                    detector.detect(resource, DetectionTarget.FACE, future.completer());
                    break;
                case AUTO:
                    detector.detect(resource, DetectionTarget.ALL, future.completer());
                    break;
                default:
                    future.complete(new Features());
            }
        }

        BufferedImage source = image.getSource();
        int width = source.getWidth();
        int height = source.getHeight();

        return future.map(features -> features.resize(width, height)).map(features -> image.withFeatures(features.getFeatures()));
    }
}
