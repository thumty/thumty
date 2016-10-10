package org.eightlog.thumty.server.params;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbCropTest {

    @Test
    public void shouldCheckCanParse() throws Exception {
        assertThat(ThumbCrop.canParse("x:x")).isTrue();
        assertThat(ThumbCrop.canParse("x:x0")).isTrue();
        assertThat(ThumbCrop.canParse("x:0x")).isTrue();
        assertThat(ThumbCrop.canParse("x0:x")).isTrue();
        assertThat(ThumbCrop.canParse("0x:x")).isTrue();
        assertThat(ThumbCrop.canParse("1x1:1x1")).isTrue();
        assertThat(ThumbCrop.canParse("1.1x1.1:1.1x1.1")).isTrue();

        assertThat(ThumbCrop.canParse("x")).isFalse();
        assertThat(ThumbCrop.canParse("x:")).isFalse();
        assertThat(ThumbCrop.canParse("x:x:")).isFalse();
        assertThat(ThumbCrop.canParse("x0..1:x")).isFalse();
        assertThat(ThumbCrop.canParse("0,1x:x")).isFalse();
        assertThat(ThumbCrop.canParse("1x1:1x-1")).isFalse();
    }

    @Test
    public void shouldParse() throws Exception {
        assertThat(ThumbCrop.parse("x:x")).isEqualTo(new ThumbCrop(0, 0, 0, 0));
        assertThat(ThumbCrop.parse("x:x1")).isEqualTo(new ThumbCrop(0, 0, 0, 1));
        assertThat(ThumbCrop.parse("x:1x")).isEqualTo(new ThumbCrop(0, 0, 1, 0));
        assertThat(ThumbCrop.parse("x1:x")).isEqualTo(new ThumbCrop(0, 1, 0, 0));
        assertThat(ThumbCrop.parse("1x:x")).isEqualTo(new ThumbCrop(1, 0, 0, 0));
        assertThat(ThumbCrop.parse("1x1:1x1")).isEqualTo(new ThumbCrop(1, 1, 1, 1));
        assertThat(ThumbCrop.parse("1.1x1.1:1.1x1.1")).isEqualTo(new ThumbCrop(1.1f, 1.1f, 1.1f, 1.1f));
    }

    @Test
    public void shouldFormat() throws Exception {
        assertThat(new ThumbCrop(0, 0, 0, 0).toString()).isEqualTo("0.0x0.0:0.0x0.0");
        assertThat(new ThumbCrop(0, 0, 0, 1.1f).toString()).isEqualTo("0.0x0.0:0.0x1.1");
        assertThat(new ThumbCrop(0, 0, 1.1f, 0).toString()).isEqualTo("0.0x0.0:1.1x0.0");
    }
}