package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.geometry.FixedAlign;
import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteSize;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.eightlog.thumty.image.ImageAssertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ResizeToFillTest {

    @Test
    public void shouldThumbLargerImage() throws Exception {
        BufferedImage image = new ResizeToFill(new RelativeOrAbsoluteSize(20, 20), FixedAlign.CENTER).apply(create(100, 100, 10));

        assertThat(image).at(0, 0).hasColor(Color.BLACK);
        assertThat(image).at(1, 1).hasColor(Color.BLACK);
        assertThat(image).at(3, 3).hasColor(Color.WHITE);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(20, 20), FixedAlign.CENTER).apply(create(200, 100, 10));

        assertThat(image).at(0, 3).hasColor(Color.WHITE);
        assertThat(image).at(19, 3).hasColor(Color.WHITE);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(20, 20), FixedAlign.LEFT).apply(create(200, 100, 10));

        assertThat(image).at(0, 0).hasColor(Color.BLACK);
        assertThat(image).at(19, 3).hasColor(Color.WHITE);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(20, 20), FixedAlign.RIGHT).apply(create(200, 100, 10));

        assertThat(image).at(0, 3).hasColor(Color.WHITE);
        assertThat(image).at(19, 3).hasColor(Color.BLACK);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(20, 20), FixedAlign.CENTER).apply(create(100, 200, 10));

        assertThat(image).at(3, 0).hasColor(Color.WHITE);
        assertThat(image).at(3, 19).hasColor(Color.WHITE);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(20, 20), FixedAlign.TOP).apply(create(100, 200, 10));

        assertThat(image).at(3, 19).hasColor(Color.WHITE);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(20, 20), FixedAlign.BOTTOM).apply(create(100, 200, 10));

        assertThat(image).at(3, 0).hasColor(Color.WHITE);
        assertThat(image).at(3, 19).hasColor(Color.BLACK);
    }

    @Test
    public void shouldThumbSmallerImage() throws Exception {
        BufferedImage image = new ResizeToFill(new RelativeOrAbsoluteSize(200, 200), FixedAlign.CENTER).apply(create(10, 10, 1));

        assertThat(image).at(0, 0).hasSimilarColor(Color.BLACK);
        assertThat(image).at(199, 199).hasSimilarColor(Color.BLACK);
        assertThat(image).at(100, 100).hasColor(Color.WHITE);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(200, 100), FixedAlign.CENTER).apply(create(10, 10, 1));

        assertThat(image).at(0, 0).hasSimilarColor(Color.BLACK);
        assertThat(image).at(100, 0).hasColor(Color.WHITE);
        assertThat(image).at(100, 99).hasColor(Color.WHITE);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(200, 100), FixedAlign.TOP).apply(create(10, 10, 1));

        assertThat(image).at(0, 0).hasSimilarColor(Color.BLACK);
        assertThat(image).at(100, 0).hasSimilarColor(Color.BLACK);
        assertThat(image).at(100, 99).hasColor(Color.WHITE);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(200, 100), FixedAlign.BOTTOM).apply(create(10, 10, 1));

        assertThat(image).at(0, 0).hasSimilarColor(Color.BLACK);
        assertThat(image).at(100, 0).hasColor(Color.WHITE);
        assertThat(image).at(100, 99).hasSimilarColor(Color.BLACK);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(100, 200), FixedAlign.CENTER).apply(create(10, 10, 1));

        assertThat(image).at(0, 0).hasSimilarColor(Color.BLACK);
        assertThat(image).at(0, 99).hasColor(Color.WHITE);
        assertThat(image).at(99, 99).hasColor(Color.WHITE);
        assertThat(image).at(0, 199).hasSimilarColor(Color.BLACK);
        assertThat(image).at(99, 199).hasSimilarColor(Color.BLACK);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(100, 200), FixedAlign.LEFT).apply(create(10, 10, 1));

        assertThat(image).at(0, 0).hasSimilarColor(Color.BLACK);
        assertThat(image).at(0, 99).hasSimilarColor(Color.BLACK);
        assertThat(image).at(99, 99).hasColor(Color.WHITE);
        assertThat(image).at(0, 199).hasSimilarColor(Color.BLACK);
        assertThat(image).at(99, 199).hasSimilarColor(Color.BLACK);

        image = new ResizeToFill(new RelativeOrAbsoluteSize(100, 200), FixedAlign.RIGHT).apply(create(10, 10, 1));

        assertThat(image).at(0, 0).hasSimilarColor(Color.BLACK);
        assertThat(image).at(0, 99).hasColor(Color.WHITE);
        assertThat(image).at(99, 99).hasSimilarColor(Color.BLACK);
        assertThat(image).at(0, 199).hasSimilarColor(Color.BLACK);
        assertThat(image).at(99, 199).hasSimilarColor(Color.BLACK);
    }

    private BufferedImage create(int width, int height, int border) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.BLACK);
        g.fill(new Rectangle(0, 0, width, height));

        g.setColor(Color.WHITE);
        g.fill(new Rectangle(border, border, width - border * 2, height - border * 2));

        g.dispose();
        return image;
    }

}