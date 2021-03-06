package org.eightlog.thumty.feature.detector;

import com.google.common.io.Files;
import org.bytedeco.javacpp.Loader;

import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

/**
 * OpenCV profile face detector
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ProfileFaceDetector extends AbstractFaceDetector {

    private final CascadeClassifier classifier;

    /**
     * Profile face detector constructor
     *
     * @param weight the feature weight
     */
    public ProfileFaceDetector(double weight) {
        super(weight);
        try {
            File profileFaceXml = Loader.extractResource(ProfileFaceDetector.class, "/haarcascade_profileface.xml",
                    Files.createTempDir(), "classified", ".xml");
            profileFaceXml.deleteOnExit();

            classifier = new CascadeClassifier();
            classifier.load(profileFaceXml.getAbsolutePath());
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
