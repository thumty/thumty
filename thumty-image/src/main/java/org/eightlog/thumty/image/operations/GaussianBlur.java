package org.eightlog.thumty.image.operations;

import com.jhlabs.image.GaussianFilter;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class GaussianBlur implements ImageOp {

    private final float radius;

    public GaussianBlur(float radius) {
        this.radius = radius;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new GaussianFilter(radius).filter(image, null);
    }
}
