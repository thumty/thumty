package org.eightlog.thumty.image.io;

import org.eightlog.thumty.image.exif.ExifFilterUtils;
import org.eightlog.thumty.image.exif.ExifUtils;
import org.eightlog.thumty.image.exif.Orientation;
import org.eightlog.thumty.image.io.sampler.ImageSampler;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public abstract class AbstractImageInput<T> implements ImageInput<T> {

    private final static int FIRST_IMAGE_INDEX = 0;

    private final ImageSampler sampler;

    public AbstractImageInput(ImageSampler sampler) {
        this.sampler = sampler;
    }

    protected abstract ImageInputStream getImageInput(T t) throws IOException;

    @Override
    public Image read(T t) throws IOException, UnsupportedFormatException {
        try(ImageInputStream input = getImageInput(t)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new UnsupportedFormatException("Unsupported image format");
            }

            ImageReader reader = readers.next();
            reader.setInput(input);

            Orientation orientation = ExifUtils.getExifOrientation(reader, FIRST_IMAGE_INDEX);

            // Read default params
            ImageReadParam param = reader.getDefaultReadParam();

            int width = reader.getWidth(FIRST_IMAGE_INDEX);
            int height = reader.getHeight(FIRST_IMAGE_INDEX);

            int sampling = sampler.getSampling(width, height);
            param.setSourceSubsampling(sampling, sampling, 0, 0);

            BufferedImage source = reader.read(FIRST_IMAGE_INDEX, param);

            reader.dispose();

            return new Image(ExifFilterUtils.getFilterForOrientation(orientation).apply(source), reader.getFormatName());
        }
    }
}
