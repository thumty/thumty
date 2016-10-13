package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.Image;

import java.awt.image.BufferedImage;

/**
 * Abstract {@link ImageFilter} that doesn't change image shape and bounds, thus doesn't modify features.
 *
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public abstract class TranslateFilter implements ImageFilter {

    @Override
    public Image apply(Image image) {
        return image.withSource(apply(image.getSource()));
    }

    protected abstract BufferedImage apply(BufferedImage image);
}
