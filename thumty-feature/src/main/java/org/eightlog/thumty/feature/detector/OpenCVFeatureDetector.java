package org.eightlog.thumty.feature.detector;

import com.google.common.collect.ImmutableList;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.eightlog.thumty.image.geometry.Feature;
import org.eightlog.thumty.image.utils.BufferedImages;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Base OpenCV feature detector
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public abstract class OpenCVFeatureDetector implements FeatureDetector {

    public OpenCVFeatureDetector() {
        Loader.load(opencv_objdetect.class);
    }

    @Override
    public List<Feature> detect(BufferedImage image) {
        Mat source = fromBufferedImage(image);
        try {
            return detect(source);
        } finally {
            source.release();
        }
    }

    protected abstract List<Feature> detect(Mat image);

    protected List<Feature> detect(Mat image, Rectangle rectangle) {
        Mat sub = new Mat(image, new Rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height));

        List<Feature> features = detect(sub);
        features.forEach(f -> f.getShape().translate(rectangle.x, rectangle.y));

        return features;
    }

    private Mat fromBufferedImage(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return fromBufferedImageTypeGray(image);
        } else {
            return fromBufferedImageTypeGray(BufferedImages.copy(image, BufferedImage.TYPE_BYTE_GRAY));
        }
    }

    private Mat fromBufferedImageTypeGray(BufferedImage image) {
        Mat result = new Mat(image.getHeight(), image.getWidth(), CV_8UC1);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        result.data().put(data, 0, data.length);
        return result;
    }

    /**
     * Stack OpenCV detectors
     *
     * @param after the feature detector
     * @return a composite feature detector
     */
    @Override
    public FeatureDetector andThen(FeatureDetector after) {
        if (after instanceof OpenCVFeatureDetector) {
            OpenCVFeatureDetector self = this;
            OpenCVFeatureDetector cvAfter = (OpenCVFeatureDetector) after;

            return new OpenCVFeatureDetector() {
                @Override
                protected List<Feature> detect(Mat image) {
                    return ImmutableList.<Feature>builder().addAll(self.detect(image)).addAll(cvAfter.detect(image)).build();
                }
            };
        }

        return (image) -> ImmutableList.<Feature>builder().addAll(detect(image)).addAll(after.detect(image)).build();
    }

    /**
     * Stack OpenCV detectors
     *
     * @param after the feature detector
     * @return a composite feature detector
     */
    @Override
    public FeatureDetector andThenIfNotDetected(FeatureDetector after) {
        if (after instanceof OpenCVFeatureDetector) {
            OpenCVFeatureDetector self = this;
            OpenCVFeatureDetector cvAfter = (OpenCVFeatureDetector) after;

            return new OpenCVFeatureDetector() {
                @Override
                protected List<Feature> detect(Mat image) {
                    List<Feature> regions = self.detect(image);
                    return !regions.isEmpty() ? regions : cvAfter.detect(image);
                }
            };
        }

        return image -> {
            List<Feature> features = detect(image);
            return !features.isEmpty() ? features : after.detect(image);
        };
    }
}
