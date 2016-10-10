package org.eightlog.thumty.feature.detector;

import com.google.common.collect.ImmutableList;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.eightlog.thumty.feature.FeatureRegion;
import org.eightlog.thumty.feature.Features;
import org.eightlog.thumty.image.utils.BufferedImages;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public abstract class OpenCVFeatureDetector implements FeatureDetector {

    public OpenCVFeatureDetector() {
        Loader.load(opencv_objdetect.class);
    }

    @Override
    public Features detect(BufferedImage image) {
        Mat source = fromBufferedImage(image);
        try {
            return new Features(image, detect(source));
        } finally {
            source.release();
        }
    }

    protected abstract List<FeatureRegion> detect(Mat image);

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

    @Override
    public FeatureDetector andThen(FeatureDetector after) {
        if (after instanceof OpenCVFeatureDetector) {
            OpenCVFeatureDetector self = this;
            OpenCVFeatureDetector cvAfter = (OpenCVFeatureDetector) after;

            return new OpenCVFeatureDetector() {
                @Override
                protected List<FeatureRegion> detect(Mat image) {
                    return ImmutableList.<FeatureRegion>builder().addAll(self.detect(image)).addAll(cvAfter.detect(image)).build();
                }
            };
        }

        return (image) -> detect(image).compose(after.detect(image));
    }

    @Override
    public FeatureDetector andThenIfNotDetected(FeatureDetector after) {
        if (after instanceof OpenCVFeatureDetector) {
            OpenCVFeatureDetector self = this;
            OpenCVFeatureDetector cvAfter = (OpenCVFeatureDetector) after;

            return new OpenCVFeatureDetector() {
                @Override
                protected List<FeatureRegion> detect(Mat image) {
                    List<FeatureRegion> regions = self.detect(image);
                    return !regions.isEmpty() ? regions : cvAfter.detect(image);
                }
            };
        }

        return image -> {
            Features features = detect(image);
            return !features.isEmpty() ? features : after.detect(image);
        };
    }
}
