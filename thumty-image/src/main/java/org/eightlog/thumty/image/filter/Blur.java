package org.eightlog.thumty.image.filter;

import com.jhlabs.image.BlurFilter;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Blur extends TranslateFilter {

    public Blur() {
    }

    @Override
    protected BufferedImage apply(BufferedImage image) {
        return new BlurFilter().filter(image, image);
    }
}
