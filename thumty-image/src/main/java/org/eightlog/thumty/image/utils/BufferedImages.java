package org.eightlog.thumty.image.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class BufferedImages {

    public static BufferedImage copy(BufferedImage image) {
        return copy(image, image.getType());
    }

    public static BufferedImage copy(BufferedImage image, int type) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), type);
        Graphics graphics = copy.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return copy;
    }
}
