package org.eightlog.thumty.image.io;

import java.awt.image.BufferedImage;

/**
 * ImageSource is wrapper class for {@link BufferedImage} with information about original image size, orientation and format.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Image {

    private final BufferedImage bufferedImage;

    private final String format;

    private final float quality;

    /**
     * Image source constructor
     *
     * @param bufferedImage the buffered image
     * @param format        the original image format
     * @param quality       the original image quality
     */
    public Image(BufferedImage bufferedImage, String format, float quality) {
        this.bufferedImage = bufferedImage;
        this.format = format;
        this.quality = quality;
    }

    /**
     * Image source constructor
     *
     * @param bufferedImage the buffered image
     * @param format        the original image format
     */
    public Image(BufferedImage bufferedImage, String format) {
        this(bufferedImage, format, 0);
    }

    /**
     * Get buffered image instance
     *
     * @return the buffered image
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    /**
     * The original image format
     *
     * @return the original image format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Get image quality
     *
     * @return the image quality
     */
    public float getQuality() {
        return quality;
    }

    public Image quality(float quality) {
        return new Image(bufferedImage, format, quality);
    }

    public Image format(String format) {
        return new Image(bufferedImage, format, quality);
    }

    public Image bufferedImage(BufferedImage bufferedImage) {
        return new Image(bufferedImage, format, quality);
    }
}
