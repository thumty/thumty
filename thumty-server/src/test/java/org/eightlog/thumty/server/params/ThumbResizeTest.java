package org.eightlog.thumty.server.params;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbResizeTest {

    @Test
    public void shouldCheckCanParse() throws Exception {
        assertThat(ThumbResize.canParse("fit-in")).isTrue();
        assertThat(ThumbResize.canParse("fill")).isTrue();
        assertThat(ThumbResize.canParse("fiT-in")).isTrue();
        assertThat(ThumbResize.canParse("fIll")).isTrue();

        assertThat(ThumbResize.canParse("fills")).isFalse();
        assertThat(ThumbResize.canParse("fits")).isFalse();
    }

    @Test
    public void shouldParse() throws Exception {
        assertThat(ThumbResize.parse("fit-in")).isEqualTo(ThumbResize.FIT);
        assertThat(ThumbResize.parse("fill")).isEqualTo(ThumbResize.FILL);
    }
}