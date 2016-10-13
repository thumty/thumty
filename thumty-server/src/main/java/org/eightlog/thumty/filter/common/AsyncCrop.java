package org.eightlog.thumty.filter.common;

import io.vertx.core.Vertx;
import org.eightlog.thumty.filter.AbstractAsyncFilter;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.filter.Crop;
import org.eightlog.thumty.image.geometry.Insets;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class AsyncCrop extends AbstractAsyncFilter {

    private final Insets insets;

    public AsyncCrop(Vertx vertx, Insets insets) {
        super(vertx);
        this.insets = insets;
    }

    @Override
    protected Image applyBlocking(Image image) {
        return new Crop(insets).apply(image);
    }
}
