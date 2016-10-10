package org.eightlog.thumty.server.params;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbFilters {

    private final static String PREFIX = "filters:";

    private final Map<String, ThumbFilter> filters;

    public ThumbFilters(Map<String, ThumbFilter> filters) {
        this.filters = filters;
    }

    public static boolean canParse(String text) {
        return text.toLowerCase().startsWith(PREFIX);
    }

    public static ThumbFilters parse(String text) {
        if (canParse(text)) {
            Map<String, ThumbFilter> filters = new HashMap<>();

            String allFilters = text.substring(PREFIX.length());
            List<String> declarations = Splitter.on(":").trimResults().omitEmptyStrings().splitToList(allFilters);

            for (String declaration : declarations) {
                if (ThumbFilter.canParse(declaration)) {
                    ThumbFilter filter = ThumbFilter.parse(declaration);
                    filters.put(filter.getName(), filter);
                }
            }

            return new ThumbFilters(filters);
        }

        throw new IllegalArgumentException("Invalid filters format");
    }

    public Map<String, ThumbFilter> getFilters() {
        return filters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThumbFilters)) return false;
        ThumbFilters that = (ThumbFilters) o;
        return Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters);
    }

    @Override
    public String toString() {
        return PREFIX + Joiner.on(":").join(filters.values().stream().map(Object::toString).collect(Collectors.toList()));
    }
}
