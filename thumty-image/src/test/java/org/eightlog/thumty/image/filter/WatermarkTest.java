package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.geometry.Positions;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.eightlog.thumty.image.ImageAssertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class WatermarkTest {

    @Test
    public void shouldPlaceWatermark() throws Exception {
        BufferedImage watermark = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        BufferedImage image = ImageIO.read(new File("src/test/resources/original.jpg"));

        BufferedImage result = new Watermark(Positions.TOP_CENTER, watermark, 1).apply(image);

        assertThat(result).matches(
                new int[][]{
                        new int[]{1, 1, 1},
                        new int[]{0, 1, 0},
                        new int[]{1, 0, 0},
                }
        );
    }
}