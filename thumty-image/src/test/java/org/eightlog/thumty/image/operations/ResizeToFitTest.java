package org.eightlog.thumty.image.operations;

import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteSize;
import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.eightlog.thumty.image.ImageAssertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ResizeToFitTest {

    @Test
    public void shouldResizeLargerImage() throws Exception {
        BufferedImage image = new ResizeToFit(new RelativeOrAbsoluteSize(100, 100)).apply(create(400, 400));
        assertThat(image).hasWidth(100).hasHeight(100);

        image = new ResizeToFit(new RelativeOrAbsoluteSize(100, 100)).apply(create(400, 200));
        assertThat(image).hasWidth(100).hasHeight(50);

        image = new ResizeToFit(new RelativeOrAbsoluteSize(100, 100)).apply(create(200, 400));
        assertThat(image).hasWidth(50).hasHeight(100);
    }

    @Test
    public void shouldResizeSmallerImage() throws Exception {
        BufferedImage image = new ResizeToFit(new RelativeOrAbsoluteSize(100, 100)).apply(create(20, 20));
        assertThat(image).hasWidth(100).hasHeight(100);

        image = new ResizeToFit(new RelativeOrAbsoluteSize(100, 100)).apply(create(20, 10));
        assertThat(image).hasWidth(100).hasHeight(50);

        image = new ResizeToFit(new RelativeOrAbsoluteSize(100, 100)).apply(create(10, 20));
        assertThat(image).hasWidth(50).hasHeight(100);
    }

    private BufferedImage create(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
}