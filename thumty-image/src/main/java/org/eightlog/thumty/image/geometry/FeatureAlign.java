package org.eightlog.thumty.image.geometry;

import java.awt.*;
import java.util.List;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FeatureAlign implements Align {

    private final List<Feature> features;

    public FeatureAlign(List<Feature> features) {
        this.features = features;
    }

    @Override
    public Point calculate(Dimension source, Dimension target) {
        return new Point(calculateX(source.width, target.width), calculateY(source.height, target.height));
    }

    /**
     * Calculates x offset
     *
     * @param sourceWidth the source width
     * @param targetWidth the target width
     * @return a x offset
     */
    private int calculateX(int sourceWidth, int targetWidth) {
        if (sourceWidth <= targetWidth) {
            return (sourceWidth - targetWidth) / 2;
        } else {
            double[] integralWeights = integralHorizontalWeights(sourceWidth);

            int offset = 0;
            double weight = 0;

            for (int i = 0; i < sourceWidth - targetWidth; i++) {
                double sum = integralWeights[i] - integralWeights[i + targetWidth];

                if (sum > weight) {
                    offset = i;
                    weight = sum;
                }
            }

            return offset;
        }
    }

    /**
     * Calculates y offset
     *
     * @param sourceHeight the source height
     * @param targetHeight the target height
     * @return a y offset
     */
    private int calculateY(int sourceHeight, int targetHeight) {
        if (sourceHeight <= targetHeight) {
            return (sourceHeight - targetHeight) / 2;
        } else {

            double[] integralWeights = integralVerticalWeights(sourceHeight);

            int offset = 0;
            double weight = 0;

            for (int i = 0; i < sourceHeight - targetHeight; i++) {
                double sum = integralWeights[i] - integralWeights[i + targetHeight];

                if (sum > weight) {
                    offset = i;
                    weight = sum;
                }
            }
            return offset;
        }
    }

    /**
     * Return integral sum of feature weights for target height
     *
     * @param height the target height
     * @return a weight integral sum
     */
    private double[] integralVerticalWeights(int height) {
        double[] weights = new double[height];

        for (int y = height - 1; y >= 0; y--) {
            for (Feature feature : features) {
                if (feature.getShape().y <= y && (feature.getShape().y + feature.getShape().height) >= y) {
                    weights[y] += feature.getShape().getWidth() * feature.getWeight();
                }
            }
            if (y + 1 < height) {
                weights[y] += weights[y + 1];
            }
        }

        return weights;
    }

    /**
     * Return integral sum of feature weights for target width
     *
     * @param width the target width
     * @return a weight integral sum
     */
    private double[] integralHorizontalWeights(int width) {
        double[] weights = new double[width];

        for (int x = width - 1; x >= 0; x--) {
            for (Feature feature : features) {
                if (feature.getShape().x <= x && (feature.getShape().x + feature.getShape().width) >= x) {
                    weights[x] += feature.getShape().getHeight() * feature.getWeight();
                }
            }
            if (x + 1 < width) {
                weights[x] += weights[x + 1];
            }
        }

        return weights;
    }
}
