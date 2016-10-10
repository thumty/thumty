package org.eightlog.thumty.common.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ByteSizeParser {

    private final static Pattern PATTERN = Pattern.compile("^(?<value>\\d+(\\.\\d+)?)(?<unit>(B|KB|MB|GB|TB|PB)?)$", Pattern.CASE_INSENSITIVE);

    public static long parse(CharSequence text) {
        if (text != null) {
            Matcher matcher = PATTERN.matcher(text);
            if (matcher.matches()) {
                try {
                    float value = Float.parseFloat(matcher.group("value"));
                    String unit = matcher.group("unit");

                    switch (unit.toLowerCase()) {
                        case "pb":
                            value = value * 1000;
                        case "tb":
                            value = value * 1000;
                        case "gb":
                            value = value * 1000;
                        case "mb":
                            value = value * 1000;
                        case "kb":
                            value = value * 1000;
                    }

                    return (long) value;
                } catch (NumberFormatException ignore) {
                    // Ignore
                }
            }
        }

        return 0;
    }
}
