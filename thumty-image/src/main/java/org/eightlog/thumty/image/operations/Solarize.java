package org.eightlog.thumty.image.operations;

import com.jhlabs.image.SolarizeFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Solarize implements ImageOp {

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new SolarizeFilter().filter(image, null);
    }
}
