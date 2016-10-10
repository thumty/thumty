package org.eightlog.thumty.image.operations;

import org.eightlog.thumty.image.geometry.Insets;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * An {@link ImageOp} which crop the original image.
 * <p>
 * Will return empty 1x1 image if croping is over bounded.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Crop implements ImageOp {

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
    public BufferedImage apply(BufferedImage image) {
        Rectangle source = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        Rectangle rectangle = insets.calculate(source);

        if (!rectangle.intersects(source)) {
            return new BufferedImage(1, 1, image.getType());
        }

        return image.getSubimage(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

}
