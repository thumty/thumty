package org.eightlog.thumty.filter.common;

import io.vertx.core.Vertx;
import org.eightlog.thumty.filter.AbstractAsyncFilter;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.filter.ResizeToFill;
import org.eightlog.thumty.image.geometry.*;
import org.eightlog.thumty.server.params.ThumbAlign;
import org.eightlog.thumty.server.params.ThumbAlignType;

import java.util.Collections;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class AsyncResizeToFill extends AbstractAsyncFilter {

    private final Size size;

    private final ThumbAlign align;

    public AsyncResizeToFill(Vertx vertx, Size size, ThumbAlign align) {
        super(vertx);
        this.size = size;
        this.align = align;
    }

    @Override
    protected Image applyBlocking(Image image) {
        return new ResizeToFill(size, getAlign(image)).apply(image);
    }

    private Align getAlign(Image image) {
        if (align != null) {
            if (align.getType() == ThumbAlignType.FACE) {
                return getFaceAlign(image, align.getNumber());
            }

            if (align.getType() == ThumbAlignType.AUTO) {
                return getAutoAlign(image);
            }

            if (align.getType() == ThumbAlignType.FOCUS) {
                return new FocusAlign(new RelativeOrAbsoluteCoordinate(align.getX(), align.getY()));
            }

            switch (align.getType()) {
                case BOTTOM:
                    return FixedAlign.BOTTOM;
                case TOP:
                    return FixedAlign.TOP;
                case LEFT:
                    return FixedAlign.LEFT;
                case RIGHT:
                    return FixedAlign.RIGHT;
            }
        }

        return FixedAlign.CENTER;
    }

    private Align getFaceAlign(Image image, int num) {
        int index = num < 1 ? 0 : num - 1;
        Feature face = image.getFeatures().stream().filter(f -> f.getType() == FeatureType.FACE).skip(index).findFirst().orElse(null);

        return face == null ? FixedAlign.CENTER : new FeatureAlign(Collections.singletonList(face));
    }

    private Align getAutoAlign(Image image) {
        return new FeatureAlign(image.getFeatures());
    }
}
