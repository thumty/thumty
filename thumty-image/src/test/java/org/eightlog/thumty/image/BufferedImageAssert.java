package org.eightlog.thumty.image;

import org.fest.assertions.api.AbstractAssert;
import org.fest.assertions.api.Assertions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class BufferedImageAssert extends AbstractAssert<BufferedImageAssert, BufferedImage> {

    private static final double COLOR_SIMILARITY_THREADHOLD = 10;

    private int x;
    private int y;

    public BufferedImageAssert(BufferedImage actual) {
        super(actual, BufferedImageAssert.class);
    }

    public static BufferedImageAssert assertThat(BufferedImage image) {
        return new BufferedImageAssert(image);
    }

    public BufferedImageAssert at(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public BufferedImageAssert hasColor(Color color) {
        isNotNull();

        Assertions.assertThat(actual.getRGB(x, y))
                .overridingErrorMessage("Expected color at (%d, %d) to be equal to %s, actual %s", x, y,
                        format(color.getRGB()), format(actual.getRGB(x, y))).isEqualTo(color.getRGB());

        return this;
    }

    public BufferedImageAssert hasSimilarColor(Color color) {
        isNotNull();

        Assertions.assertThat(diff(new Color(actual.getRGB(x, y)), color))
                .overridingErrorMessage("Expected color at (%d, %d) to be similar to %s, actual %s", x, y,
                        format(color.getRGB()), format(actual.getRGB(x, y))).isLessThan(COLOR_SIMILARITY_THREADHOLD);

        return this;
    }

    public BufferedImageAssert hasWidth(int width) {
        isNotNull();

        Assertions.assertThat(actual.getWidth())
                .overridingErrorMessage("Expected width to be equal to %d, actual %d", width, actual.getWidth())
                .isEqualTo(width);

        return this;
    }

    public BufferedImageAssert hasHeight(int height) {
        isNotNull();

        Assertions.assertThat(actual.getHeight())
                .overridingErrorMessage("Expected height to be equal to %d, actual %d", height, actual.getHeight())
                .isEqualTo(height);

        return this;
    }

    public BufferedImageAssert matches(int[][] pattern) {
        if (pattern.length == 0 || pattern[0].length == 0) {
            throw new IllegalArgumentException("Invalid pattern size");
        }

        int width = actual.getWidth();
        int height = actual.getHeight();

        float lineHeight = height / pattern.length;
        float rowWidth = width / pattern[0].length;

        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                int y = (int)(i * lineHeight + .5 * lineHeight);
                int x = (int)(j * rowWidth + .5 * rowWidth);

                Color c = pattern[i][j] == 0 ? Color.WHITE : Color.BLACK;

                Assertions.assertThat(actual.getRGB(x, y))
                        .overridingErrorMessage("Expected color at (%d, %d) to be equal to %s, actual %s", x, y,
                                format(c.getRGB()), format(actual.getRGB(x, y))).isEqualTo(c.getRGB());
            }
        }

        return this;
    }

    public BufferedImageAssert matches(float[] pattern) {
        if (pattern.length != 9) {
            throw new IllegalArgumentException("Pattern must be of length 9.");
        }

        int width = actual.getWidth();
        int height = actual.getHeight();

        List<Point> points = Arrays.asList(
                new Point(0, 0), new Point(width / 2 - 1, 0), new Point(width - 1, 0),
                new Point(0, height / 2 - 1), new Point(width / 2 - 1, height / 2 - 1), new Point(width - 1, height / 2 - 1),
                new Point(0, height - 1), new Point(width / 2 - 1, height - 1), new Point(width - 1, height - 1)
        );

        for (int i = 0; i < 9; i++) {
            Point p = points.get(i);
            Color c = pattern[i] == 0 ? Color.white : Color.black;

            Assertions.assertThat(actual.getRGB(p.x, p.y))
                    .overridingErrorMessage("Expected color at (%d, %d) to be equal to %s, actual %s", p.x, p.y,
                            format(c.getRGB()), format(actual.getRGB(x, y))).isEqualTo(c.getRGB());
        }

        return this;
    }

    private double diff(Color colorA, Color colorB) {

        int rMean = (colorA.getRed() + colorB.getRed()) / 2;
        int r = colorA.getRed() - colorB.getRed();
        int g = colorA.getGreen() - colorB.getGreen();
        int b = colorA.getBlue() - colorB.getBlue();

        return Math.sqrt((((512 + rMean) * r * r) >> 8) + 4 * g * g + (((767 - rMean) * b * b) >> 8));
    }

    private String format(int color) {
        return String.format("#%02x%02x%02x", (color & 0x00ff0000) >> 16, (color & 0x0000ff00) >> 8, color & 0x000000ff);
    }
}
