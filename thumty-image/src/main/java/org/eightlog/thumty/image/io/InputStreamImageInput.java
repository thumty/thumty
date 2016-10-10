package org.eightlog.thumty.image.io;

import org.eightlog.thumty.image.io.sampler.DefaultSampler;
import org.eightlog.thumty.image.io.sampler.ImageSampler;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class InputStreamImageInput extends AbstractImageInput<InputStream> {

    public InputStreamImageInput() {
        this(DefaultSampler.INSTANCE);
    }

    public InputStreamImageInput(ImageSampler sampler) {
        super(sampler);
    }

    @Override
    protected ImageInputStream getImageInput(InputStream inputStream) throws IOException {
        return ImageIO.createImageInputStream(inputStream);
    }

}
