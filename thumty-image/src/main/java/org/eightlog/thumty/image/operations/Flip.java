package org.eightlog.thumty.image.operations;

import org.eightlog.thumty.image.geometry.Direction;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A {@code Flip} image filter
 * <p>
 * Flips image vertical or horizontal
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Flip implements ImageOp {

    /**
     * A {@code Flip} which will flip image vertical
     */
    public static final Flip VERTICAL = new Flip(Direction.VERTICAL);

    /**
     * A {@code Flip} which will flip image horizontal
     */
    public static final Flip HORIZONTAL = new Flip(Direction.HORIZONTAL);

    private final Direction direction;

    /**
     * Create {@code Flip} image filter
     *
     * @param direction the direction
     */
    public Flip(Direction direction) {
        this.direction = direction;
    }

    @Override
    public BufferedImage apply(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        BufferedImage result = new BufferedImage(width, height, source.getType());
        Graphics2D g = result.createGraphics();

        if (direction == Direction.HORIZONTAL) {
            g.drawImage(source, width, 0, 0, height, 0, 0, width, height, null);
        } else {
            g.drawImage(source, 0, height, width, 0, 0, 0, width, height, null);
        }

        g.dispose();
        return result;
    }
}
