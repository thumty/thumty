package org.eightlog.thumty.image.io.sampler;

import org.eightlog.thumty.image.geometry.Size;

import java.awt.*;

/**
 * Resulting size based {@link ImageSampler} sampler
 * <p>
 * Returns sampling ratio based on resulting image size.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class SizeSampler implements ImageSampler {

    private final Size size;

    /**
     * Size sampler constructor
     *
     * @param size the result image size
     */
    public SizeSampler(Size size) {
        this.size = size;
    }

    @Override
    public int getSampling(int width, int height) {
        Dimension dimension = size.calculate(width, height);

        int resultWidth = dimension.width;
        int resultHeight = dimension.height;

        double scaleX = (double) width / (double) resultWidth;
        double scaleY = (double) height / (double) resultHeight;

        return Math.max(Math.min((int)scaleX, (int)scaleY), 1);
    }
}
