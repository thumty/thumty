package org.eightlog.thumty.image.geometry;


import java.awt.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface Insets {

    /**
     * Calculates inset rectangle
     *
     * @param rectangle the original object rectangle
     * @return the inset rectangle
     */
    Rectangle calculate(Rectangle rectangle);

    default Rectangle calculate(int width, int height) {
        return calculate(new Rectangle(0, 0, width, height));
    }
}
