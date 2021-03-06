package org.eightlog.thumty.image.geometry;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Feature implements Comparable<Feature>, Serializable {

    private final Rectangle shape;

    private final double weight;

    private final FeatureType type;

    public Feature(Rectangle shape, FeatureType type, double weight) {
        this.shape = shape;
        this.weight = weight;
        this.type = type;
    }

    public Feature(Rectangle shape, double weight) {
        this(shape, FeatureType.COMMON, weight);
    }

    /**
     * Get feature shape
     * @return a feature shape
     */
    public Rectangle getShape() {
        return shape;
    }

    /**
     * Get feature weight
     * @return a feature weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Get feature type
     * @return a feature type
     */
    public FeatureType getType() {
        return type;
    }

    public Feature translate(int dx, int dy) {
        int x = shape.x;
        int y = shape.y;
        int width = shape.width;
        int height = shape.height;

        return withShape(new Rectangle(x + dx, y + dy, width, height));
    }

    public Feature scale(double dX, double dY) {
        int x = shape.x;
        int y = shape.y;
        int width = shape.width;
        int height = shape.height;

        return withShape(new Rectangle((int) (x * dX), (int) (y * dY), (int) (width * dX), (int) (height * dY)));
    }

    public Feature crop(Rectangle rectangle) {
        int x = shape.x;
        int y = shape.y;
        int width = shape.width;
        int height = shape.height;

        int nX, nY;
        int nWidth = width, nHeight = height;

        if (rectangle.x > x) {
            nX = 0;
            nWidth -= rectangle.x - x;
        } else {
            nX = x - rectangle.x;
        }

        if (x + width > rectangle.x + rectangle.width) {
            nWidth -= (x + width) - (rectangle.x + rectangle.width);
        }

        if (rectangle.y > y) {
            nY = 0;
            nHeight -= rectangle.y - y;
        } else {
            nY = y - rectangle.y;
        }

        if (y + height > rectangle.y + rectangle.height) {
            nHeight -= (y + height) - (rectangle.y + rectangle.height);
        }

        return withShape(new Rectangle(nX, nY, nWidth, nHeight));
    }

    public Feature withShape(Rectangle shape) {
        return new Feature(shape, type, weight);
    }

    public Feature withWeight(double weight) {
        return new Feature(shape, type, weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Feature)) return false;
        Feature feature = (Feature) o;
        return Double.compare(feature.weight, weight) == 0 &&
                type == feature.type &&
                Objects.equals(shape, feature.shape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shape, weight, type);
    }

    @Override
    public String toString() {
        return "Feature{" +
                "shape=" + shape +
                ", weight=" + weight +
                ", type=" + type +
                '}';
    }

    @Override
    public int compareTo(Feature o) {
        return shape.y == o.shape.y ? shape.x - o.shape.x : shape.y - o.shape.y;
    }
}
