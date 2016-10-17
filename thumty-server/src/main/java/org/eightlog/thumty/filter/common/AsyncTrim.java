package org.eightlog.thumty.filter.common;

import io.vertx.core.Vertx;
import org.eightlog.thumty.filter.AbstractAsyncFilter;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.filter.Trim;
import org.eightlog.thumty.server.params.ThumbTrim;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class AsyncTrim extends AbstractAsyncFilter {

    private final ThumbTrim trim;

    public AsyncTrim(Vertx vertx, ThumbTrim trim) {
        super(vertx);
        this.trim = trim;
    }

    @Override
    protected Image applyBlocking(Image image) {
        BufferedImage src = image.getSource();
        Point point = trim.toCoordinate().getPoint(src.getWidth(), src.getHeight());
        int color = src.getRGB(point.x, point.y);

        return new Trim(new Color(color), trim.getTolerance()).apply(image);
    }
}
