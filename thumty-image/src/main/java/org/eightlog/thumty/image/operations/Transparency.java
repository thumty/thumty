package org.eightlog.thumty.image.operations;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * An {@link ImageOp} that makes image transparent
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Transparency implements ImageOp {

    private final float alpha;

    /**
     * Create {@code Transparency} image filter
     *
     * @param alpha the transparency value between 0 and 1 inclusive
     */
    public Transparency(float alpha) {
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("The alpha must be in range [0;1] inclusive");
        }
        this.alpha = alpha;
    }

    @Override
    public BufferedImage apply(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return result;
    }
}
