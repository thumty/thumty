package org.eightlog.thumty.image.geometry;

import java.awt.*;

public interface Coordinate extends Position {

    Point getPoint(int width, int height);

    default Point calculate(int enclosingWidth, int enclosingHeight, int width,
                            int height, int insetLeft, int insetRight, int insetTop,
                            int insetBottom) {
        Point p = getPoint(enclosingWidth, enclosingHeight);
        return new Point(p.x + insetLeft, p.y + insetTop);
    }
}
