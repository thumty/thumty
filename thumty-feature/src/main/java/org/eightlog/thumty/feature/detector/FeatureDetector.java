package org.eightlog.thumty.feature.detector;

import com.google.common.collect.ImmutableList;
import org.eightlog.thumty.image.geometry.Feature;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Image features detector interface
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface FeatureDetector {

    FeatureDetector IDENTITY = (image) -> Collections.emptyList();

    /**
     * Build composite feature detector, which detects all features
     *
     * @param detectors the detectors
     * @return a composite feature detector
     */
    static FeatureDetector all(Iterable<FeatureDetector> detectors) {
        FeatureDetector composite = null;

        for (FeatureDetector detector : detectors) {
            if (composite == null) {
                composite = detector;
            } else {
                composite = composite.andThen(detector);
            }
        }

        return composite != null ? composite : IDENTITY;
    }

    static FeatureDetector all(FeatureDetector... detectors) {
        return all(Arrays.asList(detectors));
    }

    /**
     * Build composite feature detector, which detect first found features
     *
     * @param detectors the detectors
     * @return a composite feature detector
     */
    static FeatureDetector any(Iterable<FeatureDetector> detectors) {
        FeatureDetector composite = null;

        for (FeatureDetector detector : detectors) {
            if (composite == null) {
                composite = detector;
            } else {
                composite = composite.andThenIfNotDetected(detector);
            }
        }

        return composite != null ? composite : IDENTITY;
    }

    static FeatureDetector any(FeatureDetector... detectors) {
        return any(Arrays.asList(detectors));
    }

    /**
     * Detects image features
     *
     * @param image the image
     * @return an image features
     */
    List<Feature> detect(BufferedImage image);

    default FeatureDetector andThenIfNotDetected(FeatureDetector after) {
        Objects.requireNonNull(after);

        return image -> {
            List<Feature> features = detect(image);
            return !features.isEmpty() ? features : after.detect(image);
        };
    }

    default FeatureDetector andThen(FeatureDetector after) {
        Objects.requireNonNull(after);
        return (image) -> ImmutableList.<Feature>builder().addAll(detect(image)).addAll(after.detect(image)).build();
    }
}
