package org.eightlog.thumty.feature.detector;

import org.eightlog.thumty.feature.FeatureRegion;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public abstract class CascadeClassifierDetector extends OpenCVFeatureDetector {

    private final static Size DEFAULT_OBJECT_SIZE = new Size();

    private final double weight;

    public CascadeClassifierDetector(double weight) {
        this.weight = weight;
    }

    @Override
    protected List<FeatureRegion> detect(Mat image) {
        List<FeatureRegion> results = new ArrayList<>();
        RectVector detected = new RectVector();

        try {
            getClassifier().detectMultiScale(image, detected, getScaleFactor(), getMinNeighbors(),
                    CV_HAAR_SCALE_IMAGE
                            | CV_HAAR_FIND_BIGGEST_OBJECT
                            | CV_HAAR_DO_ROUGH_SEARCH, getMinSize(image.cols(), image.rows()), getMaxSize(image.cols(), image.rows()));

            for (long i = 0; i < detected.size(); i++) {
                Rect rect = detected.get(i);
                results.add(new FeatureRegion(rect.x(), rect.y(), rect.width(), rect.height(), weight));
            }

        } finally {
            detected.deallocate();
        }

        return results;
    }

    protected abstract CascadeClassifier getClassifier();

    protected Size getMinSize(int width, int height) {
        return DEFAULT_OBJECT_SIZE;
    }

    protected Size getMaxSize(int width, int height) {
        return DEFAULT_OBJECT_SIZE;
    }

    protected int getMinNeighbors() {
        return 3;
    }

    protected double getScaleFactor() {
        return 1.2;
    }
}
