package org.eightlog.thumty.feature.detector;

import com.google.common.io.Files;
import org.bytedeco.javacpp.Loader;
import org.eightlog.thumty.image.geometry.Feature;

import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class EyeGlassesDetector extends CascadeClassifierDetector {
    private final CascadeClassifier classifier;

    public EyeGlassesDetector(double weight) {
        super(3, 1.2, Feature.EYE, weight);

        try {
            File xml = Loader.extractResource(EyeGlassesDetector.class, "/haarcascade_eye_tree_eyeglasses.xml",
                    Files.createTempDir(), "classified", ".xml");
            xml.deleteOnExit();

            classifier = new CascadeClassifier();
            classifier.load(xml.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CascadeClassifier getClassifier() {
        return classifier;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        classifier.close();
    }

}
