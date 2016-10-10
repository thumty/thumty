package org.eightlog.thumty.common.text;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class DurationParser {
    private final static Pattern PATTERN = Pattern.compile("^((?<w>\\d+)w(eeks?)?\\s*)?((?<d>\\d+)d(ays?)?\\s*)?((?<h>\\d+)h(ours?)?\\s*)?((?<m>\\d+)m(in(utes?)?)?\\s*)?((?<s>\\d+)s(ec(onds?)?)?\\s*)?((?<ms>\\d+)ms\\s*)?$", Pattern.CASE_INSENSITIVE);

    public static long parse(CharSequence text, long defaultValue) {
        if (text != null) {
            Matcher matcher = PATTERN.matcher(text);

            if (matcher.matches()) {
                try {
                    int weeks = Integer.parseInt(Optional.ofNullable(matcher.group("w")).orElse("0"));
                    int days = Integer.parseInt(Optional.ofNullable(matcher.group("d")).orElse("0"));
                    int hours = Integer.parseInt(Optional.ofNullable(matcher.group("h")).orElse("0"));
                    int minutes = Integer.parseInt(Optional.ofNullable(matcher.group("m")).orElse("0"));
                    int seconds = Integer.parseInt(Optional.ofNullable(matcher.group("s")).orElse("0"));
                    int millis = Integer.parseInt(Optional.ofNullable(matcher.group("ms")).orElse("0"));

                    return TimeUnit.DAYS.toMillis(weeks * 7) + TimeUnit.DAYS.toMillis(days) +
                            TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds)
                            + millis;
                } catch (NumberFormatException ignore) {
                }
            }
        }

        return defaultValue;
    }

    public static long parse(CharSequence text) {
        return parse(text, 0);
    }

}
