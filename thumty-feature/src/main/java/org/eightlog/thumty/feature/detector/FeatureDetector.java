package org.eightlog.thumty.feature.detector;

import org.eightlog.thumty.feature.Features;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface FeatureDetector {

    FeatureDetector IDENTITY = (image) -> new Features(image, Collections.emptyList());

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
    Features detect(BufferedImage image);

    default FeatureDetector andThenIfNotDetected(FeatureDetector after) {
        Objects.requireNonNull(after);

        return image -> {
            Features features = detect(image);
            return !features.isEmpty() ? features : after.detect(image);
        };
    }

    default FeatureDetector andThen(FeatureDetector after) {
        Objects.requireNonNull(after);
        return (image) -> detect(image).compose(after.detect(image));
    }
}
