package org.eightlog.thumty.image.filter;

import org.eightlog.thumty.image.Image;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ImageFilter {
    /**
     * Identity operation, which doesn't do anything
     */
    ImageFilter IDENTITY = t -> t;

    /**
     * Compose operations
     *
     * @param operations the operations
     * @return a composite filter
     */
    static ImageFilter compose(Iterable<ImageFilter> operations) {
        ImageFilter composite = ImageFilter.IDENTITY;

        for (ImageFilter filter : operations) {
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
    static ImageFilter compose(ImageFilter... operations) {
        return compose(Arrays.asList(operations));
    }

    /**
     * Compose two filter
     *
     * @param after the after filter
     * @return an image filter
     */
    default ImageFilter andThen(ImageFilter after) {
        Objects.requireNonNull(after);
        return t -> after.apply(apply(t));
    }

    /**
     * Applies transformation to image
     *
     * @param image the original image
     * @return a result image
     */
    Image apply(Image image);
}
