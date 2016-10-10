package org.eightlog.thumty.image.operations;

import com.google.common.collect.ImmutableMap;
import org.eightlog.thumty.image.geometry.Align;
import org.eightlog.thumty.image.geometry.Size;
import org.eightlog.thumty.image.resize.BicubicResizer;
import org.eightlog.thumty.image.resize.LanczosResizer;
import org.eightlog.thumty.image.resize.Resizer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * An {@link ImageOp} which will resize original image to fill desired width and height, preserving aspect ratio.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ResizeToFill implements ImageOp {

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
    public BufferedImage apply(BufferedImage source) {
        Dimension dimension = size.calculate(source.getWidth(), source.getHeight());

        int width = dimension.width;
        int height = dimension.height;

        double scaleX = (double) source.getWidth() / (double) width;
        double scaleY = (double) source.getHeight() / (double) height;

        double scale = Math.min(scaleX, scaleY);

        Point offset = align.calculate(new Dimension((int)(source.getWidth() / scale), (int)(source.getHeight() / scale)), dimension);

        Rectangle region = new Rectangle((int)(offset.x * scale), (int)(offset.y * scale),
                (int)(scale * width), (int)(scale * height));

        Resizer resizer;

        if (scale > 1) {
            resizer = new LanczosResizer(region);
        } else {
            resizer = new BicubicResizer(region, hints);
        }

        return resizer.resize(source, new BufferedImage(width, height, source.getType()));
    }

}
