package org.eightlog.thumty.filter.common;

import io.vertx.core.Vertx;
import org.eightlog.thumty.filter.AbstractAsyncFilter;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.filter.ResizeToFit;
import org.eightlog.thumty.image.geometry.Size;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class AsyncResizeToFit extends AbstractAsyncFilter {

    private final Size size;

    public AsyncResizeToFit(Vertx vertx, Size size) {
        super(vertx);
        this.size = size;
    }

    @Override
    protected Image applyBlocking(Image image) {
        return new ResizeToFit(size).apply(image);
    }
}
