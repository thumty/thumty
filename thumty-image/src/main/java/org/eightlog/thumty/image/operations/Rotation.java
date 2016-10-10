package org.eightlog.thumty.image.operations;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * An {@link ImageOp} that rotates image to specific degree clockwise.
 * <p>
 * The resulting image is always larger than original, and fully contains original image.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Rotation implements ImageOp {

    /**
     * A {@code Rotation} which will rotate image to 90 degrees clockwise
     */
    public static final Rotation ROTATE_RIGHT_90 = new Rotation(90);

    /**
     * A {@code Rotation} which will rotate image to 90 degrees counterclockwise
     */
    public static final Rotation ROTATE_LEFT_90 = new Rotation(-90);

    /**
     * A {@code Rotation} which will rotate image to 180 degrees
     */
    public static final Rotation ROTATE_180 = new Rotation(180);

    private final double angle;
    private final Color fill;

    /**
     * Create new {@code Rotation} filter
     *
     * @param angle the rotation angle in degrees
     * @param fill  the fill color
     */
    public Rotation(double angle, Color fill) {
        this.angle = angle;
        this.fill = fill;
    }

    /**
     * Create new {@code Rotation} filter, with default fill color
     *
     * @param angle the rotation angle in degrees
     */
    public Rotation(double angle) {
        this(angle, Color.BLACK);
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = calculateWidth(image.getWidth(), image.getHeight());
        int height = calculateHeight(image.getWidth(), image.getHeight());

        BufferedImage target;

        if (fill.getAlpha() != 0x00) {
            target = new BufferedImage(width, height, image.getType());
        } else {
            target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g = target.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
        );

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor(fill);
        g.fillRect(0, 0, width, height);

        g.rotate(Math.toRadians(angle), width / 2, height / 2);

        g.drawImage(image, (width - image.getWidth()) / 2, (height - image.getHeight()) / 2, null);
        g.dispose();

        return target;
    }

    private int calculateWidth(int width, int height) {
        double alpha = Math.toRadians(angle);
        return (int) Math.abs(width * Math.cos(alpha) + height * Math.sin(alpha));
    }

    private int calculateHeight(int width, int height) {
        double alpha = Math.toRadians(angle);
        return (int) Math.abs(width * Math.sin(alpha) + height * Math.cos(alpha));
    }

}
