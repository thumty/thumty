package org.eightlog.thumty.image.io.sampler;

import org.eightlog.thumty.image.geometry.Size;

import java.awt.*;

/**
 * Resulting size based {@link ImageSampler} sampler
 * <p>
 * Returns sampling ratio based on resulting image size, the sampled image is at least two times larger than original.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class X2Sampler implements ImageSampler {

    private final Size size;

    /**
     * Size sampler constructor
     *
     * @param size the result image size
     */
    public X2Sampler(Size size) {
        this.size = size;
    }

    @Override
    public int getSampling(int width, int height) {
        Dimension dimension = size.calculate(width, height);

        int resultWidth = dimension.width;
        int resultHeight = dimension.height;

        double scaleX = (double) width / (double) resultWidth;
        double scaleY = (double) height / (double) resultHeight;

        int sampleX = (int) (Math.log(scaleX) / Math.log(2));
        int sampleY = (int) (Math.log(scaleY) / Math.log(2));

        return Math.max(Math.min(sampleX, sampleY), 1);
    }
}
