package org.eightlog.thumty.filter;

import io.vertx.core.Vertx;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.filter.ImageFilter;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ImageAsyncFilter extends AbstractAsyncFilter {

    private final ImageFilter op;

    public ImageAsyncFilter(Vertx vertx, ImageFilter op) {
        super(vertx);
        this.op = op;
    }

    @Override
    protected Image applyBlocking(Image image) {
        return op.apply(image);
    }
}
