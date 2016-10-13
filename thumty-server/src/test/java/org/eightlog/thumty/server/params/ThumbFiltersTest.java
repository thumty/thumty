package org.eightlog.thumty.server.params;

import org.junit.Test;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbFiltersTest {

    @Test
    public void shouldCheckCanParse() throws Exception {
        assertThat(ThumbFilters.canParse("filters:")).isTrue();
        assertThat(ThumbFilters.canParse("filters:filter")).isTrue();
        assertThat(ThumbFilters.canParse("filters:filter(argument)")).isTrue();

        assertThat(ThumbFilters.canParse("filters :filter(argument)")).isFalse();
        assertThat(ThumbFilters.canParse("filters")).isFalse();
    }

    @Test
    public void shouldParse() throws Exception {
        assertThat(ThumbFilters.parse("filters:")).isEqualTo(new ThumbFilters(Collections.emptyList()));
        assertThat(ThumbFilters.parse("filters:filter")).isEqualTo(new ThumbFilters(Collections.singletonList(new ThumbFilter("filter"))));
        assertThat(ThumbFilters.parse("filters:filter(argument):")).isEqualTo(new ThumbFilters(Collections.singletonList(new ThumbFilter("filter", Collections.singletonList("argument")))));
    }

    @Test
    public void shouldFormat() throws Exception {
        assertThat(new ThumbFilters(Collections.emptyList()).toString()).isEqualTo("filters:");
        assertThat(new ThumbFilters(Collections.singletonList(new ThumbFilter("filter"))).toString()).isEqualTo("filters:filter()");
        assertThat(new ThumbFilters(Collections.singletonList(new ThumbFilter("filter", Collections.singletonList("argument")))).toString()).isEqualTo("filters:filter(argument)");
    }
}