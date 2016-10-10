package org.eightlog.thumty.image.exif;

import org.eightlog.thumty.image.operations.Flip;
import org.eightlog.thumty.image.operations.ImageOp;
import org.eightlog.thumty.image.operations.Rotation;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ExifFilterUtils {

    /**
     * Get image filter for exif orientation
     *
     * @param orientation the orientation
     * @return a filter
     */
    public static ImageOp getFilterForOrientation(Orientation orientation) {
        ImageOp filter = ImageOp.IDENTITY;

        if (orientation == Orientation.TOP_RIGHT) {
            filter = Flip.HORIZONTAL;
        } else if (orientation == Orientation.BOTTOM_RIGHT) {
            filter = Rotation.ROTATE_180;
        } else if (orientation == Orientation.BOTTOM_LEFT) {
            filter = Rotation.ROTATE_180.andThen(Flip.HORIZONTAL);
        } else if (orientation == Orientation.LEFT_TOP) {
            filter = Rotation.ROTATE_RIGHT_90.andThen(Flip.HORIZONTAL);
        } else if (orientation == Orientation.RIGHT_TOP) {
            filter = Rotation.ROTATE_RIGHT_90;
        } else if (orientation == Orientation.RIGHT_BOTTOM) {
            filter = Rotation.ROTATE_LEFT_90.andThen(Flip.HORIZONTAL);
        } else if (orientation == Orientation.LEFT_BOTTOM) {
            filter = Rotation.ROTATE_LEFT_90;
        }

        return filter;
    }
}
