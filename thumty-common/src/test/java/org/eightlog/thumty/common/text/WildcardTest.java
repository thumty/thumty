package org.eightlog.thumty.common.text;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class WildcardTest {

    @Test
    public void shouldMatchWildcard() throws Exception {
        assertThat(Wildcard.compile("a*b").matches("aaabbb")).isTrue();
        assertThat(Wildcard.compile("*b").matches("aaabbb")).isTrue();
        assertThat(Wildcard.compile("a*").matches("aaabbb")).isTrue();
        assertThat(Wildcard.compile("a*").matches("сaaabbb")).isFalse();
        assertThat(Wildcard.compile("*b").matches("сaaabbbd")).isFalse();
        assertThat(Wildcard.compile("a*b*c").matches("azzbzzc")).isTrue();
        assertThat(Wildcard.compile("a*b*c").matches("azzbzzcz")).isFalse();
        assertThat(Wildcard.compile("a*b*c.+").matches("azzbzzcz")).isFalse();
        assertThat(Wildcard.compile("a*b*c.+").matches("azzbzzc.+")).isTrue();
    }
}