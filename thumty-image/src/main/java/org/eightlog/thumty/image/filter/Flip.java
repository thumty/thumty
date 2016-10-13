package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.geometry.Direction;
import org.eightlog.thumty.image.geometry.Feature;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@code Flip} image filter
 * <p>
 * Flips image vertical or horizontal
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Flip extends TransformFilter {

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

    @Override
    protected List<Feature> apply(List<Feature> features, Dimension size) {
        return features.stream().map(f -> flip(f, size)).collect(Collectors.toList());
    }

    private Feature flip(Feature feature, Dimension size) {
        Rectangle shape = feature.getShape();

        if (direction == Direction.HORIZONTAL) {
            return new Feature(new Rectangle(size.width - shape.x - shape.width, shape.y, shape.width, shape.height), feature.getWeight(), feature.getType());
        }

        return new Feature(new Rectangle(shape.x, size.height - shape.y - shape.height, shape.width, shape.height), feature.getWeight(), feature.getType());
    }
}
