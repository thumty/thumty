package org.eightlog.thumty.image.operations;

import com.jhlabs.image.GrayscaleFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Grayscale implements ImageOp {

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new GrayscaleFilter().filter(image, null);
    }
}
