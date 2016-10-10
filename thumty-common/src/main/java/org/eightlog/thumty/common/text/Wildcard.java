package org.eightlog.thumty.common.text;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Simple wildcard implementation
 *
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Wildcard {

    private final Pattern pattern;

    /**
     * Construct new {@code Wildcard} using pattern
     *
     * @param pattern the wildcard pattern
     * @param flags   the pattern flags, {@see Pattern}
     */
    public Wildcard(String pattern, int flags) {

        List<String> parts = new ArrayList<>();

        for (String part : Splitter.on("*").split(pattern)) {
            parts.add(Pattern.quote(part));
        }

        this.pattern = Pattern.compile("^" + Joiner.on("(.*)").join(parts) + "$", flags);
    }

    /**
     * Construct new {@code Wildcard} using pattern
     *
     * @param pattern the wildcard pattern
     */
    public Wildcard(String pattern) {
        this(pattern, 0);
    }

    /**
     * Construct new {@code Wildcard} using pattern
     *
     * @param pattern the wildcard pattern
     * @return a wildcard
     */
    public static Wildcard compile(String pattern) {
        return new Wildcard(pattern);
    }

    /**
     * Construct new {@code Wildcard} using pattern
     *
     * @param pattern the wildcard pattern
     * @param flags   the pattern flags, {@see Pattern}
     * @return a wildcard
     */
    public static Wildcard compile(String pattern, int flags) {
        return new Wildcard(pattern, flags);
    }

    /**
     * Check whether text matches wildcard
     *
     * @param text a text to inspect
     * @return true if matches, false otherwise
     */
    public boolean matches(CharSequence text) {
        return pattern.matcher(text).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wildcard)) return false;
        Wildcard wildcard = (Wildcard) o;
        return Objects.equals(pattern, wildcard.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Override
    public String toString() {
        return pattern.toString();
    }
}
