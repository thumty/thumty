package org.eightlog.thumty.image.exif;

import org.eightlog.thumty.image.filter.Flip;
import org.eightlog.thumty.image.filter.ImageFilter;
import org.eightlog.thumty.image.filter.Rotate;

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
    public static ImageFilter getFilterForOrientation(Orientation orientation) {
        ImageFilter filter = ImageFilter.IDENTITY;

        if (orientation == Orientation.TOP_RIGHT) {
            filter = Flip.HORIZONTAL;
        } else if (orientation == Orientation.BOTTOM_RIGHT) {
            filter = Rotate.ROTATE_180;
        } else if (orientation == Orientation.BOTTOM_LEFT) {
            filter = Rotate.ROTATE_180.andThen(Flip.HORIZONTAL);
        } else if (orientation == Orientation.LEFT_TOP) {
            filter = Rotate.ROTATE_RIGHT_90.andThen(Flip.HORIZONTAL);
        } else if (orientation == Orientation.RIGHT_TOP) {
            filter = Rotate.ROTATE_RIGHT_90;
        } else if (orientation == Orientation.RIGHT_BOTTOM) {
            filter = Rotate.ROTATE_LEFT_90.andThen(Flip.HORIZONTAL);
        } else if (orientation == Orientation.LEFT_BOTTOM) {
            filter = Rotate.ROTATE_LEFT_90;
        }

        return filter;
    }
}
