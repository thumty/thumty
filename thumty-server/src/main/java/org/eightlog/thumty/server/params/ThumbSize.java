package org.eightlog.thumty.server.params;

import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteSize;
import org.eightlog.thumty.image.geometry.Size;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumbSize {

    private final static Pattern PATTERN = Pattern.compile("^(?<width>-?\\d+(\\.\\d+)?)?x(?<height>-?\\d+(\\.\\d+)?)?$", Pattern.CASE_INSENSITIVE);

    private final float width;

    private final float height;

    public ThumbSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public static boolean canParse(String text) {
        return PATTERN.matcher(text).matches();
    }

    public static ThumbSize parse(String text) {
        Matcher matcher = PATTERN.matcher(text);

        if (matcher.matches()) {
            float width = 0, height = 0;

            if (matcher.group("width") != null) {
                try {
                    width = Float.parseFloat(matcher.group("width"));
                } catch (NumberFormatException ignore) {
                    // Ignore
                }
            }

            if (matcher.group("height") != null) {
                try {
                    height = Float.parseFloat(matcher.group("height"));
                } catch (NumberFormatException ignore) {
                    // Ignore
                }
            }

            return new ThumbSize(width, height);
        }

        throw new IllegalStateException("Invalid thumb size format");
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThumbSize)) return false;
        ThumbSize thumbSize = (ThumbSize) o;
        return Float.compare(thumbSize.width, width) == 0 &&
                Float.compare(thumbSize.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }

    public Size toImageSize() {
        return new RelativeOrAbsoluteSize(Math.abs(width), Math.abs(height));
    }
}
