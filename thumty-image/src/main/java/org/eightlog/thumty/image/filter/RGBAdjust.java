package org.eightlog.thumty.image.filter;

import com.jhlabs.image.RGBAdjustFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class RGBAdjust extends TranslateFilter {

    private final float red;
    private final float green;
    private final float blue;

    public RGBAdjust(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new RGBAdjustFilter(red, green ,blue).filter(image, image);
    }
}
