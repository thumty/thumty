package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.utils.ColorConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Trim implements ImageFilter {

    private final static ColorConverter COLOR_CONVERTER = new ColorConverter();

    private final double color[];

    private final float tolerance;

    public Trim(Color color, float tolerance) {
        this.color = COLOR_CONVERTER.RGBtoLAB(color.getRGB());
        this.tolerance = tolerance;
    }

    @Override
    public Image apply(Image image) {
        BufferedImage src = image.getSource();
        Rectangle bounds = new Rectangle(0, 0, src.getWidth(), src.getHeight());

        Rectangle trim = getBounds(src);

        if (!trim.intersects(bounds)) {
            return image.withSource(new BufferedImage(1, 1, src.getType())).withFeatures(Collections.emptyList());
        }

        return image
                .withSource(src.getSubimage(trim.x, trim.y, trim.width, trim.height))
                .withFeatures(
                        image.getFeatures().stream()
                                .map(f -> f.crop(trim))
                                .collect(Collectors.toList())
                );
    }


    private Rectangle getBounds(BufferedImage image) {
        int left = image.getWidth();
        int top = image.getHeight();
        int right = 0;
        int bottom = 0;

        int[] pixels = new int[image.getWidth()];

        for (int i = 0; i < image.getHeight(); i++) {
            pixels = image.getRGB(0, i, image.getWidth(), 1, pixels, 0, image.getWidth());

            int j;

            for (j = 0; j < image.getWidth(); j++) {
                if (diff(pixels[j]) > tolerance) {
                    if (j < left) {
                        left = j;
                    }

                    if (i < top) {
                        top = i;
                    }

                    bottom = i;
                    break;
                }
            }

            int min = j > right ? j : right;

            for (j = image.getWidth() - 1; j > min; j--) {
                if (diff(pixels[j]) > tolerance) {
                    if (j > right) {
                        right = j;
                    }
                    break;
                }
            }
        }

        return new Rectangle(left, top, right - left, bottom - top);
    }

    private double diff(int target) {
        double[] lab = COLOR_CONVERTER.RGBtoLAB(target);

        return Math.sqrt(
                (lab[0] - color[0]) * (lab[0] - color[0]) +
                        (lab[1] - color[1]) * (lab[1] - color[1]) +
                        (lab[2] - color[2]) * (lab[2] - color[2])
        );
    }
}
