package org.eightlog.thumty.image.filter;

import com.jhlabs.image.SolarizeFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Solarize extends TranslateFilter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new SolarizeFilter().filter(image, image);
    }
}
