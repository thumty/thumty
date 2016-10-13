package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.geometry.Feature;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public abstract class TransformFilter implements ImageFilter {

    @Override
    public Image apply(Image image) {
        BufferedImage src = image.getSource();
        Dimension size = new Dimension(src.getWidth(), src.getHeight());
        List<Feature> features = image.getFeatures();

        return image.withSource(apply(src)).withFeatures(apply(features, size));
    }

    protected abstract BufferedImage apply(BufferedImage image);

    protected abstract List<Feature> apply(List<Feature> features, Dimension size);

}
