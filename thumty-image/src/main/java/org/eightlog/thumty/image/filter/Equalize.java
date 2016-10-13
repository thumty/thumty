package org.eightlog.thumty.image.filter;

import com.jhlabs.image.EqualizeFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Equalize extends TranslateFilter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new EqualizeFilter().filter(image, image);
    }
}
