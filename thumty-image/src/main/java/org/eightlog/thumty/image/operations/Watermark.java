package org.eightlog.thumty.image.operations;

import org.eightlog.thumty.image.geometry.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * An {@link ImageOp} which applies watermark to the image
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Watermark implements ImageOp {

    /**
     * The position of the watermark.
     */
    private final Position position;

    /**
     * The watermark image.
     */
    private final BufferedImage watermarkImg;

    /**
     * The opacity of the watermark.
     */
    private final float opacity;

    /**
     * Create {@code Watermark} instance
     *
     * @param position     the watermark position
     * @param watermarkImg the watermark image
     * @param opacity      the watermark opacity
     */
    public Watermark(Position position, BufferedImage watermarkImg, float opacity) {
        Objects.requireNonNull(position, "position can't be null");
        Objects.requireNonNull(watermarkImg, "image can't be null");

        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("Opacity is out of range of " +
                    "between 0.0f and 1.0f.");
        }

        this.position = position;
        this.watermarkImg = watermarkImg;
        this.opacity = opacity;
    }

    @Override
    public BufferedImage apply(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int type = img.getType();

        BufferedImage imgWithWatermark = new BufferedImage(width, height, type);

        int watermarkWidth = watermarkImg.getWidth();
        int watermarkHeight = watermarkImg.getHeight();

        Point p = position.calculate(
                width, height, watermarkWidth, watermarkHeight,
                0, 0, 0, 0
        );

        Graphics2D g = imgWithWatermark.createGraphics();

        // Draw the actual image.
        g.drawImage(img, 0, 0, null);

        // Draw the watermark on top.
        g.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
        );

        g.drawImage(watermarkImg, p.x, p.y, null);

        g.dispose();

        return imgWithWatermark;
    }
}
