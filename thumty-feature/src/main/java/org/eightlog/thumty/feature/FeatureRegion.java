package org.eightlog.thumty.feature;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@DataObject(generateConverter = true)
public class FeatureRegion implements Comparable<FeatureRegion>, Serializable {

    private int x;
    private int y;
    private int width;
    private int height;
    private double weight;

    public FeatureRegion() {
    }

    public FeatureRegion(JsonObject json) {
        FeatureRegionConverter.fromJson(json, this);
    }

    public FeatureRegion(int x, int y, int width, int height, double weight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.weight = weight;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        FeatureRegionConverter.toJson(this, json);
        return json;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public FeatureRegion scale(double dX, double dY) {
        return new FeatureRegion((int) (x * dX), (int) (y * dY), (int) (width * dX), (int) (height * dY), weight);
    }

    public FeatureRegion crop(Rectangle rectangle) {
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

        return new FeatureRegion(nX, nY, nWidth, nHeight, weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeatureRegion)) return false;
        FeatureRegion that = (FeatureRegion) o;
        return x == that.x &&
                y == that.y &&
                width == that.width &&
                height == that.height &&
                Double.compare(that.weight, weight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height, weight);
    }

    @Override
    public int compareTo(FeatureRegion other) {
        return x == other.x ? y - other.y : x - other.x;
    }

    @Override
    public String toString() {
        return "FeatureRegion{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", weight=" + weight +
                '}';
    }
}
