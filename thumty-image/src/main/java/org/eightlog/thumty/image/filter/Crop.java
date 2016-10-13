package org.eightlog.thumty.image.filter;


import org.eightlog.thumty.image.geometry.Feature;
import org.eightlog.thumty.image.geometry.Insets;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@link ImageFilter} which crop the original image.
 * <p>
 * Will return empty 1x1 image if croping is over bounded.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Crop extends TransformFilter {

    private final Insets insets;

    /**
     * Create {@code Crop} image filter
     *
     * @param insets the crop inset
     */
    public Crop(Insets insets) {
        this.insets = insets;
    }


    @Override
    protected BufferedImage apply(BufferedImage image) {
        Rectangle source = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        Rectangle rectangle = insets.calculate(source);

        if (!rectangle.intersects(source)) {
            return new BufferedImage(1, 1, image.getType());
        }

        return image.getSubimage(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    protected List<Feature> apply(List<Feature> features, Dimension size) {
        Rectangle source = new Rectangle(0, 0, size.width, size.height);
        Rectangle rectangle = insets.calculate(source);

        if (!rectangle.intersects(source)) {
            return Collections.emptyList();
        }

        return features.stream().map(f -> f.crop(rectangle)).collect(Collectors.toList());
    }
}
