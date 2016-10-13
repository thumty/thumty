package org.eightlog.thumty.image.filter;

import com.jhlabs.image.GrayscaleFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Grayscale extends TranslateFilter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new GrayscaleFilter().filter(image, image);
    }
}
