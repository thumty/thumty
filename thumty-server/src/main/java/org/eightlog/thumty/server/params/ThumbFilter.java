package org.eightlog.thumty.server.params;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbFilter {

    private final static Pattern PATTERN = Pattern.compile("^(?<name>[a-zA-Z_]+)(\\((?<args>[^\\)]*)\\))?$", Pattern.CASE_INSENSITIVE);

    private final String name;

    private final List<String> arguments;

    public ThumbFilter(String name) {
        this(name, Collections.emptyList());
    }

    public ThumbFilter(String name, List<String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public static boolean canParse(String text) {
        return PATTERN.matcher(text).matches();
    }

    public static ThumbFilter parse(String text) {
        Matcher matcher = PATTERN.matcher(text);

        if (matcher.matches()) {
            return new ThumbFilter(matcher.group("name"), parseArguments(matcher.group("args")));
        }

        throw new IllegalArgumentException("Invalid filter format");
    }

    private static List<String> parseArguments(@Nullable String text) {
        if (text == null) {
            return Collections.emptyList();
        }

        return Splitter.on(",").trimResults().omitEmptyStrings().splitToList(text)
                .stream().map(ThumbFilter::decode).collect(Collectors.toList());
    }

    private static String decode(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThumbFilter)) return false;
        ThumbFilter that = (ThumbFilter) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arguments);
    }

    @Override
    public String toString() {
        return name + "(" + Joiner.on(",").join(arguments.stream().map(ThumbFilter::encode).collect(Collectors.toList())) + ")";
    }

    private static String encode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
