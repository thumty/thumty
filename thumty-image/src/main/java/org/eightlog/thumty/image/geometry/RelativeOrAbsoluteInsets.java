package org.eightlog.thumty.image.geometry;

import java.awt.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class RelativeOrAbsoluteInsets implements Insets {

    private final float left;
    private final float right;
    private final float top;
    private final float bottom;

    public RelativeOrAbsoluteInsets(float left, float right, float top, float bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public RelativeOrAbsoluteInsets(double left, double right, double top, double bottom) {
        this((float)left, (float)right, (float)top, (float)bottom);
    }

    public RelativeOrAbsoluteInsets(int left, int right, int top, int bottom) {
        this((float)left, (float)right, (float)top, (float)bottom);
    }

    @Override
    public Rectangle calculate(Rectangle rectangle) {
        int left = absolute(rectangle.width, this.left);
        int top = absolute(rectangle.height, this.top);
        int right = absolute(rectangle.width, this.right);
        int bottom = absolute(rectangle.height, this.bottom);

        return new Rectangle(rectangle.x + left, rectangle.y + top, rectangle.width - left - right, rectangle.height - top - bottom);
    }

    private int absolute(int length, float value) {
        if (value > 0 && value < 1) {
            return (int) (length * value);
        }
        return (int) value;
    }
}
