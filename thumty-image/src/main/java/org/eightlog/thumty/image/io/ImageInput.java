package org.eightlog.thumty.image.io;

import org.eightlog.thumty.image.Image;

import java.io.IOException;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ImageInput<T> {

    /**
     * Read {@link Image} from resource of type T
     *
     * @param t the source
     * @return a source image
     * @throws IOException                on IO exception
     * @throws UnsupportedFormatException if format of image is not supported
     */
    Image read(T t) throws IOException, UnsupportedFormatException;
}
