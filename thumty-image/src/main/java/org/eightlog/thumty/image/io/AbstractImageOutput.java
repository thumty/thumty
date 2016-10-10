package org.eightlog.thumty.image.io;

import org.eightlog.thumty.image.utils.BufferedImages;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public abstract class AbstractImageOutput implements ImageOutput {

    protected abstract ImageOutputStream getImageOutputStream() throws IOException;

    @Override
    public void write(BufferedImage image, String format, String compressionType, float quality)
            throws IOException, UnsupportedFormatException {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(format, "format must not be null");

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);

        if (!writers.hasNext()) {
            throw new UnsupportedFormatException("Unsupported output format " + format);
        }

        ImageWriter writer = writers.next();

        // Get default write params
        ImageWriteParam writeParam = writer.getDefaultWriteParam();

        if (writeParam.canWriteCompressed()) {
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            if (compressionType != null) {
                writeParam.setCompressionType(compressionType);
            } else {
                List<String> compressionTypes = getSupportedOutputFormatTypes(format);

                if (!compressionTypes.isEmpty()) {
                    writeParam.setCompressionType(compressionTypes.iterator().next());
                }
            }

            if (!Float.isNaN(quality)) {
                writeParam.setCompressionQuality(quality);
            }
        }

        try (ImageOutputStream os = getImageOutputStream()) {

            if (format.equalsIgnoreCase("jpg")
                    || format.equalsIgnoreCase("jpeg")
                    || format.equalsIgnoreCase("bmp")) {
                image = BufferedImages.copy(image, BufferedImage.TYPE_INT_RGB);
            }

            writer.setOutput(os);
            writer.write(null, new IIOImage(image, null, null), writeParam);
            writer.dispose();
        }
    }

    private List<String> getSupportedOutputFormatTypes(String format) {
        if (format == null) {
            return Collections.emptyList();
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            return Collections.emptyList();
        }

        String[] types;

        try {
            types = writers.next().getDefaultWriteParam().getCompressionTypes();
        } catch (UnsupportedOperationException e) {
            return Collections.emptyList();
        }

        return types != null ? Arrays.asList(types) : Collections.emptyList();
    }
}
