package org.eightlog.thumty.filter.common;

import io.vertx.core.Vertx;
import org.eightlog.thumty.filter.AbstractAsyncFilter;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.filter.Flip;
import org.eightlog.thumty.image.geometry.Direction;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class AsyncFlip extends AbstractAsyncFilter {

    private final Direction direction;

    public AsyncFlip(Vertx vertx, Direction direction) {
        super(vertx);
        this.direction = direction;
    }

    @Override
    protected Image applyBlocking(Image image) {
        return new Flip(direction).apply(image);
    }
}
