package org.eightlog.thumty.filters.sync;

import io.vertx.core.Vertx;
import org.eightlog.thumty.image.io.Image;
import org.eightlog.thumty.image.operations.ImageOp;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ImageOpAsyncFilter extends AbstractAsyncFilter {

    private final ImageOp op;

    public ImageOpAsyncFilter(Vertx vertx, ImageOp op) {
        super(vertx);
        this.op = op;
    }

    @Override
    protected Image applyBlocking(Image image) {
        return op.apply(image);
    }
}
