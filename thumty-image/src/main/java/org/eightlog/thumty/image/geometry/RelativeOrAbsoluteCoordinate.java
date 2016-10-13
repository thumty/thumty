package org.eightlog.thumty.image.geometry;

import java.awt.*;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class RelativeOrAbsoluteCoordinate implements Coordinate {

    private final float x;
    private final float y;

    public RelativeOrAbsoluteCoordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Point getPoint(int width, int height) {
        int xR = x < 0 || x > 1 ? (int)x : (int)(x * width);
        int yR = y < 0 || y > 1 ? (int)y : (int)(y * height);

        return new Point(xR, yR);
    }
}
