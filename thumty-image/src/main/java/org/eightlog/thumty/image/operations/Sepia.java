package org.eightlog.thumty.image.operations;

import com.jhlabs.image.PointFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Sepia implements ImageOp {

    private final static int DEPTH = 20;

    private final int intensity;

    public Sepia(int intensity) {
        this.intensity = intensity;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new PointFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
                int a = rgb & 0xff000000;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int gry = (r + g + b) / 3;
                r = g = b = gry;
                r += DEPTH * 2;
                g += DEPTH;

                r = r > 255 ? 255 : r;
                g = g > 255 ? 255 : g;
                b = b > 255 ? 255 : b;

                b -= intensity;

                if (b < 0) b = 0;
                if (b > 255) b = 255;

                return a | r << 16 | g << 8 | b;
            }
        }.filter(image, null);
    }
}
