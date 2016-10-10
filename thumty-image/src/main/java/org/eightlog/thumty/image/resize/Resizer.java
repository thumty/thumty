package org.eightlog.thumty.image.resize;

import java.awt.image.BufferedImage;

/**
 * Interface for implementing by image resizers.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface Resizer {

    /**
     * Resize image
     * <p>
     * The source image is resized to destination image dimensions and output to destination image.
     *
     * @param src the source image
     * @param dst the destination image
     * @return a destination image
     */
    BufferedImage resize(BufferedImage src, BufferedImage dst);

}
