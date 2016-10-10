package org.eightlog.thumty.server.params;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbFilterTest {

    @Test
    public void shouldCheckCanParse() throws Exception {
        assertThat(ThumbFilter.canParse("filter")).isTrue();
        assertThat(ThumbFilter.canParse("fil_ter")).isTrue();
        assertThat(ThumbFilter.canParse("filter()")).isTrue();
        assertThat(ThumbFilter.canParse("filter(argument)")).isTrue();
        assertThat(ThumbFilter.canParse("filter(argument, argument)")).isTrue();

        assertThat(ThumbFilter.canParse("123")).isFalse();
        assertThat(ThumbFilter.canParse("1ABC()")).isFalse();
        assertThat(ThumbFilter.canParse("x1")).isFalse();
    }

    @Test
    public void shouldParse() throws Exception {
        assertThat(ThumbFilter.parse("filter")).isEqualTo(new ThumbFilter("filter"));
        assertThat(ThumbFilter.parse("fil_ter")).isEqualTo(new ThumbFilter("fil_ter"));
        assertThat(ThumbFilter.parse("filter()")).isEqualTo(new ThumbFilter("filter"));
        assertThat(ThumbFilter.parse("filter(argument)")).isEqualTo(new ThumbFilter("filter", Collections.singletonList("argument")));
        assertThat(ThumbFilter.parse("filter(argument, argument)")).isEqualTo(new ThumbFilter("filter", Arrays.asList("argument", "argument")));
        assertThat(ThumbFilter.parse("filter(argument, argument, %5B%5Dvalue%2B-)")).isEqualTo(new ThumbFilter("filter", Arrays.asList("argument", "argument", "[]value+-")));
    }

    @Test
    public void shouldFormat() throws Exception {
        assertThat(new ThumbFilter("filter").toString()).isEqualTo("filter()");
        assertThat(new ThumbFilter("fil_ter").toString()).isEqualTo("fil_ter()");
        assertThat(new ThumbFilter("filter", Collections.singletonList("argument")).toString()).isEqualTo("filter(argument)");
        assertThat(new ThumbFilter("filter", Arrays.asList("argument", "argument")).toString()).isEqualTo("filter(argument,argument)");
        assertThat(new ThumbFilter("filter", Arrays.asList("argument", "argument","[]value+-")).toString()).isEqualTo("filter(argument,argument,%5B%5Dvalue%2B-)");
    }
}