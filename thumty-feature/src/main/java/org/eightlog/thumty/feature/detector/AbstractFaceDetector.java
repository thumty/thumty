package org.eightlog.thumty.feature.detector;

import com.google.common.collect.ImmutableList;
import org.eightlog.thumty.image.geometry.Feature;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Size;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public abstract class AbstractFaceDetector extends CascadeClassifierDetector {

    private static final double HAIR_SIZE = 0.2;
    private static final double NECK_SIZE = 0.3;
    private static final double SIDE_SIZE = 0.1;

    private final EyeDetector eyeDetector;

    public AbstractFaceDetector(double weight) {
        super(3, 1.1, Feature.FACE, weight);
        eyeDetector = new EyeDetector(weight / 2);
    }

    @Override
    protected List<Feature> detect(Mat image) {
        List<Feature> features = super.detect(image);
        List<Feature> eyes = new ArrayList<>();

        for (Feature feature : features) {
            eyes.addAll(eyeDetector.detect(image, feature.getShape()));
        }

        return ImmutableList.<Feature>builder()
                .addAll(features.stream().map(this::addSurroundArea).collect(Collectors.toList()))
                .addAll(eyes)
                .build();
    }

    private Feature addSurroundArea(Feature feature) {
        Rectangle shape = feature.getShape();

        int x = shape.x;
        int y = shape.y;
        int width = shape.width;
        int height = shape.height;

        int hair = (int)(height * HAIR_SIZE);
        int neck = (int)(height * NECK_SIZE);
        int sides = (int)(width * SIDE_SIZE);

        Rectangle area = new Rectangle(Math.max(0, x - sides / 2), Math.max(0, y - hair),
                width + sides, height + hair + neck);

        return new Feature(area, feature.getWeight(), feature.getType());
    }

    @Override
    protected Size getMinSize(int width, int height) {
        int size = Math.max(Math.min(width, height) / 15, 20);
        return new Size(size, size);
    }
}
