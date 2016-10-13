package org.eightlog.thumty.image.filter;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.eightlog.thumty.image.ImageAssertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FlipTest {

    @Test
    public void shouldFlipHorizontal() throws Exception {
        BufferedImage image = ImageIO.read(new File("src/test/resources/original.jpg"));

        BufferedImage result = Flip.HORIZONTAL.apply(image);

        assertThat(result).matches(
                new float[]{
                        1, 0, 1,
                        0, 1, 0,
                        0, 0, 1
                }
        );
    }

    @Test
    public void shouldFlipVertical() throws Exception {
        BufferedImage image = ImageIO.read(new File("src/test/resources/original.jpg"));

        BufferedImage result = Flip.VERTICAL.apply(image);

        assertThat(result).matches(
                new float[]{
                        1, 0, 0,
                        0, 1, 0,
                        1, 0, 1
                }
        );
    }
}