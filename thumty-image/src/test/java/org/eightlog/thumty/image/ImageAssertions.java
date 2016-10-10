package org.eightlog.thumty.image;

import org.fest.assertions.api.Assertions;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ImageAssertions extends Assertions {

    public static BufferedImageAssert assertThat(BufferedImage image) {
        return new BufferedImageAssert(image);
    }
}
