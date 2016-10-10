package org.eightlog.thumty.image.operations;

import com.jhlabs.image.BlurFilter;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Blur implements ImageOp {

    public Blur() {
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new BlurFilter().filter(image, null);
    }
}
