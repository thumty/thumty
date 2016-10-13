package org.eightlog.thumty.image;

import org.eightlog.thumty.image.geometry.Feature;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

/**
 * Image is wrapper class for {@link BufferedImage} with information about original image size, orientation and format.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Image {

    private final BufferedImage source;

    private final String format;

    private final float quality;

    private final List<Feature> features;

    public Image(BufferedImage source, String format, float quality, List<Feature> features) {
        this.source = source;
        this.format = format;
        this.quality = quality;
        this.features = features;
    }

    /**
     * Image source constructor
     *
     * @param source the buffered image
     * @param format        the original image format
     * @param quality       the original image quality
     */
    public Image(BufferedImage source, String format, float quality) {
        this(source, format, quality, Collections.emptyList());
    }

    /**
     * Image source constructor
     *
     * @param source the buffered image
     * @param format        the original image format
     */
    public Image(BufferedImage source, String format) {
        this(source, format, Float.NaN);
    }

    /**
     * Get buffered image instance
     *
     * @return the buffered image
     */
    public BufferedImage getSource() {
        return source;
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

    public List<Feature> getFeatures() {
        return features;
    }

    public Image withQuality(float quality) {
        return new Image(source, format, quality, features);
    }

    public Image withFormat(String format) {
        return new Image(source, format, quality, features);
    }

    public Image withSource(BufferedImage source) {
        return new Image(source, format, quality, features);
    }

    public Image withFeatures(List<Feature> features) {
        return new Image(source, format, quality, features);
    }
}
