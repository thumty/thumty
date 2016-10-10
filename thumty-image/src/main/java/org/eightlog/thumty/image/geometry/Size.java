package org.eightlog.thumty.image.geometry;

import java.awt.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface Size {

    /**
     * Calculates size of an object
     *
     * @param width  the width of an object which size should be determined
     * @param height the height of an object which size should be determined
     * @return calculated size of an object
     */
    Dimension calculate(int width, int height);
}
