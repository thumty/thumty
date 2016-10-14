package org.eightlog.thumty.image.filter;

import com.google.common.collect.ImmutableMap;
import org.eightlog.thumty.image.geometry.Feature;
import org.eightlog.thumty.image.geometry.Size;
import org.eightlog.thumty.image.resize.BicubicResizer;
import org.eightlog.thumty.image.resize.LanczosResizer;
import org.eightlog.thumty.image.resize.Resizer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An {@link ImageFilter} which will resize original image to fit desired width and height, preserving aspect ratio.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ResizeToFit extends TransformFilter {

    private final Size size;
    private final Map<RenderingHints.Key, Object> hints;

    /**
     * Create new {@code ResizeToFit} instance
     *
     * @param size  the resulting image bounds
     * @param hints custom resize hints
     */
    public ResizeToFit(Size size, Map<RenderingHints.Key, Object> hints) {
        this.size = size;
        this.hints = hints;
    }

    /**
     * Create new {@code ResizeToFit} instance
     *
     * @param size  the resulting image bounds
     */
    public ResizeToFit(Size size) {
        this(size, ImmutableMap.of(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    }

    @Override
    protected BufferedImage apply(BufferedImage source) {
        Dimension current = new Dimension(source.getWidth(), source.getHeight());
        Dimension resulting = size.calculate(source.getWidth(), source.getHeight());

        double scale = getScale(current, resulting);

        Resizer resizer;

        if (scale > 1) {
            resizer = new LanczosResizer();
        } else {
            resizer = new BicubicResizer(hints);
        }

        return resizer.resize(source,
                new BufferedImage((int) (source.getWidth() / scale), (int) (source.getHeight() / scale), source.getType()));
    }

    @Override
    protected List<Feature> apply(List<Feature> features, Dimension current) {
        Dimension resulting = size.calculate(current.width, current.height);
        double scale = getScale(current, resulting);

        return features.stream().map(f -> f.scale(1 / scale, 1 / scale)).collect(Collectors.toList());
    }

    private double getScale(Dimension current, Dimension resulting) {
        double scaleX = current.getWidth() / resulting.getWidth();
        double scaleY = current.getHeight() / resulting.getHeight();
        return Math.max(scaleX, scaleY);
    }
}
