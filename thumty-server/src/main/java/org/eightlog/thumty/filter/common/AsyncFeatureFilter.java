package org.eightlog.thumty.filter.common;

import io.vertx.core.Vertx;
import org.eightlog.thumty.filter.AbstractAsyncFilter;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.geometry.Feature;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class AsyncFeatureFilter extends AbstractAsyncFilter {

    public AsyncFeatureFilter(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected Image applyBlocking(Image image) {
        BufferedImage src = image.getSource();

        Graphics2D g = src.createGraphics();
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.BLACK);

        for (Feature feature : image.getFeatures()) {
            g.draw(feature.getShape());
        }

        g.dispose();
        return image;
    }
}
