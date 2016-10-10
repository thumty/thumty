package org.eightlog.thumty.image.operations;

import com.google.common.collect.ImmutableMap;
import org.eightlog.thumty.image.geometry.Size;
import org.eightlog.thumty.image.resize.BicubicResizer;
import org.eightlog.thumty.image.resize.LanczosResizer;
import org.eightlog.thumty.image.resize.Resizer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * An {@link ImageOp} which will resize original image to fit desired width and height, preserving aspect ratio.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ResizeToFit implements ImageOp {

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
    public BufferedImage apply(BufferedImage source) {
        Dimension dimension = size.calculate(source.getWidth(), source.getHeight());

        int width = dimension.width;
        int height = dimension.height;

        double scaleX = (double) source.getWidth() / (double) width;
        double scaleY = (double) source.getHeight() / (double) height;

        double scale = Math.max(scaleX, scaleY);

        Resizer resizer;

        if (scale > 1) {
            resizer = new LanczosResizer();
        } else {
            resizer = new BicubicResizer(hints);
        }

        return resizer.resize(source,
                new BufferedImage((int) (source.getWidth() / scale), (int) (source.getHeight() / scale), source.getType()));
    }
}
