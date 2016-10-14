package org.eightlog.thumty.feature.detector;

import com.google.common.io.Files;
import org.bytedeco.javacpp.Loader;

import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

/**
 * OpenCV front face feature detector
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FrontFaceDetector extends AbstractFaceDetector {

    private final CascadeClassifier classifier;

    /**
     * Front face detector constructor
     *
     * @param weight the feature weight
     */
    public FrontFaceDetector(double weight) {
        super(weight);
        try {
            File frontFaceXml = Loader.extractResource(FrontFaceDetector.class, "/haarcascade_frontalface_default.xml",
                    Files.createTempDir(), "classified", ".xml");
            frontFaceXml.deleteOnExit();

            classifier = new CascadeClassifier();
            classifier.load(frontFaceXml.getAbsolutePath());
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
