package org.eightlog.thumty.image.io;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ImageOutput {

    /**
     * Check whether format is write supported
     *
     * @param format the format
     * @return true if write is supported
     */
    default boolean isWriteSupported(String format) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        return writers.hasNext();
    }

    default void write(Image image) throws IOException, UnsupportedFormatException {
        write(image.getBufferedImage(), image.getFormat(), image.getQuality());
    }

    /**
     * Write image to output with default compression
     *
     * @param image   the image to write
     * @param format  the output format
     * @param quality the output image quality
     * @throws IOException                on io exception
     * @throws UnsupportedFormatException if output format is not supported
     */
    default void write(BufferedImage image, String format, float quality) throws IOException, UnsupportedFormatException {
        write(image, format, null, quality);
    }

    /**
     * Write image to output with default compression and quality values
     *
     * @param image  the image to write
     * @param format the output format
     * @throws IOException                on io exception
     * @throws UnsupportedFormatException if output format is not supported
     */
    default void write(BufferedImage image, String format) throws IOException, UnsupportedFormatException {
        write(image, format, null, Float.NaN);
    }

    /**
     * Write image to output
     *
     * @param image           the image to write
     * @param format          the image output format
     * @param compressionType the image output compression type
     * @param quality         the image quality
     * @throws IOException                on io exception
     * @throws UnsupportedFormatException if output format is not supported
     */
    void write(BufferedImage image, String format, String compressionType, float quality) throws IOException, UnsupportedFormatException;
}
