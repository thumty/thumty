package org.eightlog.thumty.image.geometry;

import java.awt.*;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FocusAlign implements Align {

    private final Coordinate coordinate;

    public FocusAlign(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public Point calculate(Dimension source, Dimension target) {
        Point focus = coordinate.getPoint(source.width, source.height);

        // Calculate X
        int x;
        if (focus.x - (target.width / 2) < 0) {
            x = 0;
        } else if (focus.x + (target.width / 2) >= source.width) {
            x = source.width - target.width; 
        } else {
            x = focus.x - (target.width / 2);
        }
        
        // Calculate Y
        int y;
        if (focus.y - (target.height / 2) < 0) {
            y = 0;
        } else if (focus.y + (target.height / 2) >= source.height) {
            y = source.height - target.height;
        } else {
            y = focus.y - (target.height / 2);
        }

        return new Point(x, y);
    }
}
