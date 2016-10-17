package org.eightlog.thumty.server.params;

import org.eightlog.thumty.image.geometry.Coordinate;
import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteCoordinate;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumbTrim {

    private final static Pattern PATTERN = Pattern.compile("^trim(?<position>:(top-left|bottom-right))?(?<tolerance>:\\d+(\\.\\d+)?)?$", Pattern.CASE_INSENSITIVE);

    private final float x;

    private final float y;

    private final float tolerance;

    public ThumbTrim(float x, float y, float tolerance) {
        this.x = x;
        this.y = y;
        this.tolerance = tolerance;
    }

    public static boolean canParse(String text) {
        return PATTERN.matcher(text).matches();
    }

    public static ThumbTrim parse(String text) {
        Matcher matcher = PATTERN.matcher(text);

        if (matcher.matches()) {
            float x = 0, y = 0;

            if (matcher.group("position") != null) {
                String position = matcher.group("position").substring(1);

                if (position.equalsIgnoreCase("bottom-right")) {
                    x = 1;
                    y = 1;
                }
            }

            float tolerance = 0;

            if (matcher.group("tolerance") != null) {
                try {
                    tolerance = Float.parseFloat(matcher.group("tolerance").substring(1));
                }catch (NumberFormatException ignore) {
                    // Ignore
                }
            }

            return new ThumbTrim(x, y, tolerance);
        }

        throw new IllegalArgumentException("Invalid trim format");
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getTolerance() {
        return tolerance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThumbTrim)) return false;
        ThumbTrim thumbTrim = (ThumbTrim) o;
        return Float.compare(thumbTrim.x, x) == 0 &&
                Float.compare(thumbTrim.y, y) == 0 &&
                tolerance == thumbTrim.tolerance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, tolerance);
    }

    @Override
    public String toString() {
        return "trim:" + (x == y && y == 0.0 ? "top-left" : "bottom-right") + ":" + tolerance;
    }

    public Coordinate toCoordinate() {
        return new RelativeOrAbsoluteCoordinate(x, y);
    }
}
