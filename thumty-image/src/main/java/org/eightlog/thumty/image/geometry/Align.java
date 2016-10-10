package org.eightlog.thumty.image.geometry;

import java.awt.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface Align {

    /**
     * Calculates resulting position of rectangle with dimensions of target inside rectangle with dimensions of source
     *
     * @param source the source rectangle dimensions in which target rectangle is aligned
     * @param target the target rectangle dimensions which is aligned inside source rectangle
     * @return the top left corner position
     */
    Point calculate(Dimension source, Dimension target);
}
