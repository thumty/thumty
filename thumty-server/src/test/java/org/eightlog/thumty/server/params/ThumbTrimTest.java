package org.eightlog.thumty.server.params;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbTrimTest {

    @Test
    public void shouldCheckCanParse() throws Exception {
        assertThat(ThumbTrim.canParse("trim")).isTrue();
        assertThat(ThumbTrim.canParse("trim:1")).isTrue();
        assertThat(ThumbTrim.canParse("trim:1.2")).isTrue();
        assertThat(ThumbTrim.canParse("trim:top-left")).isTrue();
        assertThat(ThumbTrim.canParse("trim:top-left:1")).isTrue();
        assertThat(ThumbTrim.canParse("trim:top-left:1.2")).isTrue();
        assertThat(ThumbTrim.canParse("trim:bottom-right")).isTrue();
        assertThat(ThumbTrim.canParse("trim:bottom-right:1")).isTrue();
        assertThat(ThumbTrim.canParse("trim:bottom-right:1.2")).isTrue();

        assertThat(ThumbTrim.canParse("trim:")).isFalse();
        assertThat(ThumbTrim.canParse("trim:1.1.")).isFalse();
        assertThat(ThumbTrim.canParse("trim:1-2")).isFalse();
        assertThat(ThumbTrim.canParse("trim:topleft")).isFalse();
        assertThat(ThumbTrim.canParse("trim:top-left1")).isFalse();
        assertThat(ThumbTrim.canParse("trim:top-left 1.2")).isFalse();
        assertThat(ThumbTrim.canParse("trim:bottomright")).isFalse();
        assertThat(ThumbTrim.canParse("trim:bottom-right1")).isFalse();
        assertThat(ThumbTrim.canParse("trim:bottom-right 1.2")).isFalse();
    }

    @Test
    public void shouldParse() throws Exception {
        assertThat(ThumbTrim.parse("trim")).isEqualTo(new ThumbTrim(0, 0, 0));
        assertThat(ThumbTrim.parse("trim:1")).isEqualTo(new ThumbTrim(0, 0, 1));
        assertThat(ThumbTrim.parse("trim:1.2")).isEqualTo(new ThumbTrim(0, 0, 1.2f));
        assertThat(ThumbTrim.parse("trim:top-left")).isEqualTo(new ThumbTrim(0, 0, 0));
        assertThat(ThumbTrim.parse("trim:top-left:1")).isEqualTo(new ThumbTrim(0, 0, 1));
        assertThat(ThumbTrim.parse("trim:top-left:1.2")).isEqualTo(new ThumbTrim(0, 0, 1.2f));
        assertThat(ThumbTrim.parse("trim:bottom-right")).isEqualTo(new ThumbTrim(1, 1, 0));
        assertThat(ThumbTrim.parse("trim:bottom-right:1")).isEqualTo(new ThumbTrim(1, 1, 1));
        assertThat(ThumbTrim.parse("trim:bottom-right:1.2")).isEqualTo(new ThumbTrim(1, 1, 1.2f));
    }

    @Test
    public void shouldFormat() throws Exception {
        assertThat(new ThumbTrim(0, 0, 1).toString()).isEqualTo("trim:top-left:1.0");
        assertThat(new ThumbTrim(1, 1, 1).toString()).isEqualTo("trim:bottom-right:1.0");
        assertThat(new ThumbTrim(1, 1, 1.23f).toString()).isEqualTo("trim:bottom-right:1.23");
    }
}