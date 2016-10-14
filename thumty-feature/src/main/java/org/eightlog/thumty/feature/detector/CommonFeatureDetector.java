package org.eightlog.thumty.feature.detector;

import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.eightlog.thumty.image.geometry.Feature;
import org.eightlog.thumty.image.geometry.FeatureType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgproc.goodFeaturesToTrack;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class CommonFeatureDetector extends OpenCVFeatureDetector {

    private final double weight;

    public CommonFeatureDetector(double weight) {
        this.weight = weight;
    }

    @Override
    protected List<Feature> detect(Mat image) {
        List<Feature> result = new ArrayList<>();

        Mat corners = new Mat();

        try {
            goodFeaturesToTrack(image, corners, 20, .04, 1.0);

            FloatRawIndexer indexer = corners.createIndexer();

            long size = indexer.rows() * indexer.cols();

            for (long i = 0; i < size; i++) {
                int x = (int) indexer.get(i);
                int y = (int) indexer.get(i + 1);

                result.add(new Feature(new Rectangle(x, y, 1, 1), FeatureType.COMMON, weight));
            }

            return result;
        } finally {
            corners.release();
        }
    }
}
