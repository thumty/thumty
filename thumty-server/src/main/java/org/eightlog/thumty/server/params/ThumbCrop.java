package org.eightlog.thumty.server.params;

import org.eightlog.thumty.image.geometry.Insets;
import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteInsets;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumbCrop {
    private final static Pattern PATTERN = Pattern.compile("^(?<top>\\d+(\\.\\d+)?)?x(?<left>\\d+(\\.\\d+)?)?:(?<bottom>\\d+(\\.\\d+)?)?x(?<right>\\d+(\\.\\d+)?)?$");

    private final float top;
    private final float left;
    private final float bottom;
    private final float right;

    public ThumbCrop(float top, float left, float bottom, float right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public static boolean canParse(String text) {
        return PATTERN.matcher(text).matches();
    }

    public static ThumbCrop parse(String text) {
        Matcher matcher = PATTERN.matcher(text);

        if (matcher.matches()) {
            float top = 0, left = 0, bottom = 0, right = 0;

            if (matcher.group("top") != null) {
                try {
                    top = Float.parseFloat(matcher.group("top"));
                } catch (NumberFormatException ignore) {
                    // Ignore
                }
            }

            if (matcher.group("left") != null) {
                try {
                    left = Float.parseFloat(matcher.group("left"));
                } catch (NumberFormatException ignore) {
                    // Ignore
                }
            }

            if (matcher.group("bottom") != null) {
                try {
                    bottom = Float.parseFloat(matcher.group("bottom"));
                } catch (NumberFormatException ignore) {
                    // Ignore
                }
            }

            if (matcher.group("right") != null) {
                try {
                    right = Float.parseFloat(matcher.group("right"));
                } catch (NumberFormatException ignore) {
                    // Ignore
                }
            }

            return new ThumbCrop(top, left, bottom, right);
        }

        throw new IllegalArgumentException("Invalid crop format");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThumbCrop)) return false;
        ThumbCrop thumbCrop = (ThumbCrop) o;
        return Float.compare(thumbCrop.top, top) == 0 &&
                Float.compare(thumbCrop.left, left) == 0 &&
                Float.compare(thumbCrop.bottom, bottom) == 0 &&
                Float.compare(thumbCrop.right, right) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, left, bottom, right);
    }

    @Override
    public String toString() {
        return top + "x" + left + ":" + bottom + "x" + right;
    }

    public float getTop() {
        return top;
    }

    public float getLeft() {
        return left;
    }

    public float getBottom() {
        return bottom;
    }

    public float getRight() {
        return right;
    }

    public Insets toInsets() {
        return new RelativeOrAbsoluteInsets(left, right, top, bottom);
    }
}
