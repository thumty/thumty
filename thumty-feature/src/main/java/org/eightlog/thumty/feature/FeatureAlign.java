package org.eightlog.thumty.feature;

import org.eightlog.thumty.image.geometry.Align;

import java.awt.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FeatureAlign implements Align {

    private final Features features;

    public FeatureAlign(Features features) {
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
        double scale = (double)features.getHeight() / (double)height;

        double[] weights = new double[height];

        for (int i = height - 1; i >= 0; i--) {
            double y = (double) i * scale;
            for (FeatureRegion region : features.getRegions()) {
                if (region.getY() <= y && (region.getY() + region.getHeight()) >= y) {
                    weights[i] += region.getWeight();
                }
            }
            if (i + 1 < height) {
                weights[i] += weights[i + 1];
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
        double scale = (double)features.getWidth() / (double)width;

        double[] weights = new double[width];

        for (int i = width - 1; i >= 0; i--) {
            double x = (double)i * scale;
            for (FeatureRegion region : features.getRegions()) {
                if (region.getX() <= x && (region.getX() + region.getWidth()) >= x) {
                    weights[i] += region.getWeight();
                }
            }
            if (i + 1 < width) {
                weights[i] += weights[i + 1];
            }
        }

        return weights;
    }
}
