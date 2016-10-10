package org.eightlog.thumty.server.params;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbSizeTest {

    @Test
    public void shouldCheckCanParse() throws Exception {
        assertThat(ThumbSize.canParse("x")).isTrue();
        assertThat(ThumbSize.canParse("0x")).isTrue();
        assertThat(ThumbSize.canParse("x1")).isTrue();
        assertThat(ThumbSize.canParse("1x1")).isTrue();
        assertThat(ThumbSize.canParse("10.22x10.22")).isTrue();
        assertThat(ThumbSize.canParse("22x10.22")).isTrue();

        assertThat(ThumbSize.canParse("22 x 10.22")).isFalse();
        assertThat(ThumbSize.canParse("1,2x10")).isFalse();
        assertThat(ThumbSize.canParse("1..2x10")).isFalse();
        assertThat(ThumbSize.canParse("1.2.3x10")).isFalse();
        assertThat(ThumbSize.canParse("2222")).isFalse();
        assertThat(ThumbSize.canParse("unsupported")).isFalse();
    }

    @Test
    public void shouldParse() throws Exception {
        assertThat(ThumbSize.parse("x")).isEqualTo(new ThumbSize(0, 0));
        assertThat(ThumbSize.parse("1x")).isEqualTo(new ThumbSize(1, 0));
        assertThat(ThumbSize.parse("x1")).isEqualTo(new ThumbSize(0, 1));
        assertThat(ThumbSize.parse("15x12")).isEqualTo(new ThumbSize(15, 12));
        assertThat(ThumbSize.parse("15.12x12.15")).isEqualTo(new ThumbSize(15.12f, 12.15f));
    }

    @Test
    public void shouldFormat() throws Exception {
        assertThat(new ThumbSize(1, 1).toString()).isEqualTo("1.0x1.0");
        assertThat(new ThumbSize(1, 1.1f).toString()).isEqualTo("1.0x1.1");
        assertThat(new ThumbSize(0, 0).toString()).isEqualTo("0.0x0.0");
    }
}