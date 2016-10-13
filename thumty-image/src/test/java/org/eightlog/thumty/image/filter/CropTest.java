package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteInsets;
import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.eightlog.thumty.image.ImageAssertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class CropTest {

    @Test
    public void shouldCropRatio() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage result = new Crop(new RelativeOrAbsoluteInsets(0.1, 0.4, 0.2, 0.5)).apply(image);
        assertThat(result).hasWidth(50).hasHeight(30);
    }

    @Test
    public void shouldCropFixed() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage result = new Crop(new RelativeOrAbsoluteInsets(10, 40, 20, 50)).apply(image);
        assertThat(result).hasWidth(50).hasHeight(30);
    }

    @Test
    public void shouldCropTo1x1Fixed() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage result = new Crop(new RelativeOrAbsoluteInsets(50, 50, 20, 30)).apply(image);
        assertThat(result).hasWidth(1).hasHeight(1);
    }

    @Test
    public void shouldCropTo1x1Ratio() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage result = new Crop(new RelativeOrAbsoluteInsets(10, 10, .5, .5)).apply(image);
        assertThat(result).hasWidth(1).hasHeight(1);
    }
}