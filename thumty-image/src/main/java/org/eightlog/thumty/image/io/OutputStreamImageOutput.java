package org.eightlog.thumty.image.io;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class OutputStreamImageOutput extends AbstractImageOutput {

    private final OutputStream outputStream;

    public OutputStreamImageOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    protected ImageOutputStream getImageOutputStream() throws IOException {
        return ImageIO.createImageOutputStream(outputStream);
    }
}
