package org.eightlog.thumty.image.operations;

import org.eightlog.thumty.image.io.Image;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ImageOp {
    /**
     * Identity operation, which doesn't do anything
     */
    ImageOp IDENTITY = t -> t;

    /**
     * Compose operations
     *
     * @param operations the operations
     * @return a composite filter
     */
    static ImageOp compose(Iterable<ImageOp> operations) {
        ImageOp composite = ImageOp.IDENTITY;

        for (ImageOp filter : operations) {
            composite = composite.andThen(filter);
        }

        return composite;
    }

    /**
     * Compose operations
     *
     * @param operations the operations
     * @return a composite filter
     */
    static ImageOp compose(ImageOp... operations) {
        return compose(Arrays.asList(operations));
    }

    /**
     * Compose two filter
     *
     * @param after the after filter
     * @return an image filter
     */
    default ImageOp andThen(ImageOp after) {
        Objects.requireNonNull(after);
        return t -> after.apply(apply(t));
    }

    /**
     * Applies transformation to image
     *
     * @param image the original image
     * @return a result image
     */
    BufferedImage apply(BufferedImage image);

    default Image apply(Image image) {
        return image.bufferedImage(apply(image.getBufferedImage()));
    }
}
