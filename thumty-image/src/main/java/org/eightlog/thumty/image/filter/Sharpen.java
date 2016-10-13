package org.eightlog.thumty.image.filter;

import com.jhlabs.image.SharpenFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Sharpen extends TranslateFilter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new SharpenFilter().filter(image, image);
    }
}
