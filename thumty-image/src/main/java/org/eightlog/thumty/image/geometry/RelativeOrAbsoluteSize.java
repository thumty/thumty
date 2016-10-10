package org.eightlog.thumty.image.geometry;

import java.awt.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class RelativeOrAbsoluteSize implements Size {

    private final float width;
    private final float height;

    public RelativeOrAbsoluteSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public RelativeOrAbsoluteSize(double width, double height) {
        this((float)width, (float)height);
    }

    public RelativeOrAbsoluteSize(int width, int height) {
        this((float)width, (float)height);
    }

    @Override
    public Dimension calculate(int width, int height) {
        int w, h;

        if (this.height >= 1) {
            h = (int)this.height;
        } else if (this.height > 0 && this.height < 1) {
            h = (int) (this.height * height);
        } else {
            h = height;
        }

        if (this.width >= 1) {
            w = (int)this.width;
        } else if (this.width > 0 && this.width < 1) {
            w = (int) (this.width * width);
        } else {
            w = height;
        }

        return new Dimension(w, h);
    }
}
