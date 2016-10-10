package org.eightlog.thumty.server.params;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumbAlign {

    private final static Pattern PATTERN = Pattern.compile("^(?<side>left|top|right|bottom|center|auto|features)|(?<face>face(:\\d+)?)|(?<focus>focus:(\\d+(\\.\\d+)?)x(\\d+(\\.\\d+)?))$", Pattern.CASE_INSENSITIVE);

    private final ThumbAlignType type;

    private final int number;

    private final float x;

    private final float y;

    public ThumbAlign(ThumbAlignType type, int number, float x, float y) {
        this.type = type;
        this.number = number;
        this.x = x;
        this.y = y;
    }

    public ThumbAlign(ThumbAlignType type) {
        this(type, 0, 0, 0);
    }

    public ThumbAlign(int n) {
        this(ThumbAlignType.FACE, n, 0, 0);
    }

    public ThumbAlign(float x, float y) {
        this(ThumbAlignType.FOCUS, 0, x, y);
    }

    public ThumbAlignType getType() {
        return type;
    }

    public int getNumber() {
        return number;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        switch (type) {
            case FACE:
                return type.toString().toLowerCase() + ":" + number;
            case FOCUS:
                return type.toString().toLowerCase() + ":" + x + "x" + y;
            default:
                return type.toString().toLowerCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThumbAlign)) return false;
        ThumbAlign that = (ThumbAlign) o;
        return number == that.number &&
                Float.compare(that.x, x) == 0 &&
                Float.compare(that.y, y) == 0 &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number, x, y);
    }

    public static boolean canParse(String text) {
        return PATTERN.matcher(text).matches();
    }

    public static ThumbAlign parse(String text) {
        Matcher matcher = PATTERN.matcher(text);

        if (matcher.matches()) {
            if (matcher.group("side") != null) {
                return parseNonParametrized(matcher.group("side"));
            } else if (matcher.group("face") != null){
                return parseFace(matcher.group("face"));
            } else if (matcher.group("focus") != null) {
                return parseFocus(matcher.group("focus"));
            }
        }

        throw new IllegalStateException("Can't parse align from " + text);
    }

    private static ThumbAlign parseNonParametrized(String text) {
        String value = text.toLowerCase();

        switch (value) {
            case "left":
                return new ThumbAlign(ThumbAlignType.LEFT);
            case "right":
                return new ThumbAlign(ThumbAlignType.RIGHT);
            case "top":
                return new ThumbAlign(ThumbAlignType.TOP);
            case "bottom":
                return new ThumbAlign(ThumbAlignType.BOTTOM);
            case "center":
                return new ThumbAlign(ThumbAlignType.CENTER);
            case "auto":
                return new ThumbAlign(ThumbAlignType.AUTO);
        }

        return null;
    }

    private static ThumbAlign parseFace(String text) {
        if (text.contains(":")) {
            String index = text.substring(text.indexOf(":") + 1);
            try{
                return new ThumbAlign(Integer.parseInt(index));
            } catch (NumberFormatException ignore) {
                return new ThumbAlign(0);
            }
        } else {
            return new ThumbAlign(0);
        }
    }

    private static ThumbAlign parseFocus(String text) {
        String value = text.toLowerCase();

        String coordinates = value.substring(value.indexOf(":") + 1);

        String x = coordinates.substring(0, coordinates.indexOf("x"));
        String y = coordinates.substring(coordinates.indexOf("x") + 1);

        try{
            return new ThumbAlign(Float.parseFloat(x), Float.parseFloat(y));
        } catch (NumberFormatException ignore) {
            return new ThumbAlign(0.5f, 0.5f);
        }
    }
}
