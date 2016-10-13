package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.geometry.Feature;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@link ImageFilter} that rotates image to specific degree clockwise.
 * <p>
 * The resulting image is always larger than original, and fully contains original image.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Rotate extends TransformFilter {

    /**
     * A {@code Rotation} which will rotate image to 90 degrees clockwise
     */
    public static final Rotate ROTATE_RIGHT_90 = new Rotate(90);

    /**
     * A {@code Rotation} which will rotate image to 90 degrees counterclockwise
     */
    public static final Rotate ROTATE_LEFT_90 = new Rotate(-90);

    /**
     * A {@code Rotation} which will rotate image to 180 degrees
     */
    public static final Rotate ROTATE_180 = new Rotate(180);

    private final double angle;
    private final Color fill;

    /**
     * Create new {@code Rotation} filter
     *
     * @param angle the rotation angle in degrees
     * @param fill  the fill color
     */
    public Rotate(double angle, Color fill) {
        this.angle = angle;
        this.fill = fill;
    }

    /**
     * Create new {@code Rotation} filter, with default fill color
     *
     * @param angle the rotation angle in degrees
     */
    public Rotate(double angle) {
        this(angle, Color.BLACK);
    }

    @Override
    public org.eightlog.thumty.image.Image apply(org.eightlog.thumty.image.Image image) {
        Graphics2D g1 = image.getSource().createGraphics();
        g1.setColor(Color.RED);
        g1.setStroke(new BasicStroke(2));
        for (Feature feature : image.getFeatures()) {
            g1.drawRect(feature.getShape().x, feature.getShape().y, feature.getShape().width, feature.getShape().height);
        }
        g1.dispose();

        org.eightlog.thumty.image.Image result = super.apply(image);
        Graphics2D g = result.getSource().createGraphics();

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        for (Feature feature : result.getFeatures()) {
            g.drawRect(feature.getShape().x, feature.getShape().y, feature.getShape().width, feature.getShape().height);
        }
        g.dispose();

        return result;
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

    @Override
    protected List<Feature> apply(List<Feature> features, Dimension size) {
        int width = calculateWidth(size.width, size.height);
        int height = calculateHeight(size.width, size.height);

        AffineTransform transform = new AffineTransform();
        transform.translate(((double)width - size.getWidth()) / 2, ((double)height - size.getHeight()) / 2);

        transform.translate(size.getWidth() / 2, size.getHeight() / 2);
        transform.rotate(Math.toRadians(angle));
        transform.translate(-size.getWidth() / 2, -size.getHeight() / 2);

        return features.stream().map(f -> rotate(f, transform)).collect(Collectors.toList());
    }

    private Feature rotate(Feature feature, AffineTransform transform) {
        Rectangle shape = transform.createTransformedShape(feature.getShape()).getBounds();
        return new Feature(shape, feature.getWeight(), feature.getType());
    }
}
