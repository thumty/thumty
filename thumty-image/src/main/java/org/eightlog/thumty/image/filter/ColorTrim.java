package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.Image;

import java.awt.*;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ColorTrim implements ImageFilter {

    private final Color color;

    private final float tolerance;

    public ColorTrim(Color color, float tolerance) {
        this.color = color;
        this.tolerance = tolerance;
    }

    @Override
    public Image apply(Image image) {
        return null;
    }

}
