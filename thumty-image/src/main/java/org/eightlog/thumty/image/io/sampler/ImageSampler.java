package org.eightlog.thumty.image.io.sampler;

import javax.imageio.ImageReadParam;

/**
 * Image sampler.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ImageSampler {

    /**
     * Get image sampling ratio based on source image width and height
     *
     * @param width  the source width
     * @param height the source height
     * @return a sampling ratio for X and Y as in {@link ImageReadParam}
     */
    int getSampling(int width, int height);
}
