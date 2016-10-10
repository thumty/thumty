package org.eightlog.thumty.image.resize;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public abstract class AbstractResizer implements Resizer {

    private final Rectangle sourceRegion;

    public AbstractResizer() {
        this(null);
    }

    public AbstractResizer(@Nullable Rectangle sourceRegion) {
        this.sourceRegion = sourceRegion;
    }

    /**
     * @return a rendering hints for resize graphics
     */
    protected abstract Map<RenderingHints.Key, Object> getRenderingHints();

    /**
     * Provides absolute resize region of the source image.
     *
     * @return a source resize region
     */
    protected Rectangle getSourceRegion(BufferedImage image) {
        return sourceRegion != null ? sourceRegion.getBounds() : new Rectangle(0, 0, image.getWidth(), image.getHeight());
    }

    protected BufferedImage getSourceImage(BufferedImage image) {
        Rectangle region = getSourceRegion(image);

        if (!region.equals(image.getRaster().getBounds())) {
            BufferedImage result = new BufferedImage(region.width, region.height, image.getType());

            Graphics2D g = result.createGraphics();
            g.drawImage(image,
                    0,               0,            region.width,            region.height,
                    region.x, region.y, region.x + region.width, region.y + region.height, null);
            g.dispose();
            return result;
        }

        return image;
    }

    /**
     * Perform simple resize.
     * <p>
     * Apply hints, provided in constructor
     *
     * @param src the source image
     * @param dst the destination image
     * @return a destination image
     */
    @Override
    public BufferedImage resize(BufferedImage src, BufferedImage dst) {
        Rectangle region = getSourceRegion(src);

        Graphics2D g = dst.createGraphics();
        g.setRenderingHints(getRenderingHints());
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, dst.getWidth(), dst.getHeight(),
                region.x, region.y, region.x + region.width, region.y + region.height, null);
        g.dispose();
        return dst;
    }

}
