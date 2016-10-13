package org.eightlog.thumty.image.filter;

import com.google.common.collect.ImmutableMap;
import org.eightlog.thumty.image.geometry.Align;
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
 * An {@link ImageFilter} which will resize original image to fill desired width and height, preserving aspect ratio.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ResizeToFill extends TransformFilter {

    private final Size size;
    private final Align align;
    private final Map<RenderingHints.Key, Object> hints;

    /**
     * Create {@code ResizeToFill} image filter instance
     *
     * @param size  the thumb size
     * @param align the original image align
     * @param hints the custom rendering hints
     */
    public ResizeToFill(Size size, Align align, Map<RenderingHints.Key, Object> hints) {
        this.size = size;
        this.align = align;
        this.hints = hints;
    }

    /**
     * Create {@code ResizeToFill} image filter instance
     *
     * @param size  the thumb size
     * @param align the original image align
     */
    public ResizeToFill(Size size, Align align) {
        this(size, align, ImmutableMap.of(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    }

    @Override
    protected BufferedImage apply(BufferedImage source) {
        Dimension current = new Dimension(source.getWidth(), source.getHeight());
        Dimension resulting = size.calculate(source.getWidth(), source.getHeight());

        double scale = getScale(current, resulting);
        Point offset = getOffset(current, resulting);

        Rectangle region = new Rectangle((int)(offset.x * scale), (int)(offset.y * scale),
                (int)(scale * resulting.width), (int)(scale * resulting.height));

        Resizer resizer;

        if (scale > 1) {
            resizer = new LanczosResizer(region);
        } else {
            resizer = new BicubicResizer(region, hints);
        }

        return resizer.resize(source, new BufferedImage(resulting.width, resulting.height, source.getType()));
    }

    @Override
    protected List<Feature> apply(List<Feature> features, Dimension current) {
        Dimension resulting = size.calculate(current.width, current.height);

        double scale = getScale(current, resulting);
        Point offset = getOffset(current, resulting);

        Rectangle region = new Rectangle((int)(offset.x * scale), (int)(offset.y * scale),
                (int)(scale * resulting.width), (int)(scale * resulting.height));

        return features.stream()
                .map(f -> f.crop(region))
                .map(f -> f.scale(scale, scale))
                .collect(Collectors.toList());
    }

    private double getScale(Dimension current, Dimension resulting) {
        double scaleX = current.getWidth() / resulting.getWidth();
        double scaleY = current.getHeight() / resulting.getHeight();
        return Math.min(scaleX, scaleY);
    }

    private Point getOffset(Dimension current, Dimension resulting) {
        double scale = getScale(current, resulting);

        return align.calculate(new Dimension((int)(current.getWidth() / scale), (int)(current.getHeight() / scale)), resulting);
    }

}
