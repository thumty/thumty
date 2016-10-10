package org.eightlog.thumty.server.params;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbAlignTest {

    @Test
    public void shouldCheckCanParse() throws Exception {
        assertThat(ThumbAlign.canParse("left")).isTrue();
        assertThat(ThumbAlign.canParse("right")).isTrue();
        assertThat(ThumbAlign.canParse("top")).isTrue();
        assertThat(ThumbAlign.canParse("bottom")).isTrue();
        assertThat(ThumbAlign.canParse("center")).isTrue();
        assertThat(ThumbAlign.canParse("auto")).isTrue();
        assertThat(ThumbAlign.canParse("face")).isTrue();
        assertThat(ThumbAlign.canParse("face:1")).isTrue();
        assertThat(ThumbAlign.canParse("focus:1x1")).isTrue();
        assertThat(ThumbAlign.canParse("focus:1.2x1.2")).isTrue();

        assertThat(ThumbAlign.canParse("lEft")).isTrue();
        assertThat(ThumbAlign.canParse("rIght")).isTrue();
        assertThat(ThumbAlign.canParse("tOp")).isTrue();
        assertThat(ThumbAlign.canParse("bOttom")).isTrue();
        assertThat(ThumbAlign.canParse("cEnter")).isTrue();
        assertThat(ThumbAlign.canParse("aUto")).isTrue();
        assertThat(ThumbAlign.canParse("fAce")).isTrue();
        assertThat(ThumbAlign.canParse("fAce:1")).isTrue();
        assertThat(ThumbAlign.canParse("fOcus:1x1")).isTrue();
        assertThat(ThumbAlign.canParse("fOcus:1.2x1.2")).isTrue();

        assertThat(ThumbAlign.canParse("left:1")).isFalse();
        assertThat(ThumbAlign.canParse("ri-ght")).isFalse();
        assertThat(ThumbAlign.canParse("to.p")).isFalse();
        assertThat(ThumbAlign.canParse("botom")).isFalse();
        assertThat(ThumbAlign.canParse("cnter")).isFalse();
        assertThat(ThumbAlign.canParse("to")).isFalse();
        assertThat(ThumbAlign.canParse("face:1.1")).isFalse();
        assertThat(ThumbAlign.canParse("face1")).isFalse();
        assertThat(ThumbAlign.canParse("focus")).isFalse();
        assertThat(ThumbAlign.canParse("focus:1.1.1x1")).isFalse();
        assertThat(ThumbAlign.canParse("focus:1.2")).isFalse();
    }

    @Test
    public void shouldParse() throws Exception {
        assertThat(ThumbAlign.parse("left")).isEqualTo(new ThumbAlign(ThumbAlignType.LEFT));
        assertThat(ThumbAlign.parse("right")).isEqualTo(new ThumbAlign(ThumbAlignType.RIGHT));
        assertThat(ThumbAlign.parse("top")).isEqualTo(new ThumbAlign(ThumbAlignType.TOP));
        assertThat(ThumbAlign.parse("bottom")).isEqualTo(new ThumbAlign(ThumbAlignType.BOTTOM));
        assertThat(ThumbAlign.parse("center")).isEqualTo(new ThumbAlign(ThumbAlignType.CENTER));
        assertThat(ThumbAlign.parse("auto")).isEqualTo(new ThumbAlign(ThumbAlignType.AUTO));
        assertThat(ThumbAlign.parse("face")).isEqualTo(new ThumbAlign(0));
        assertThat(ThumbAlign.parse("face:1")).isEqualTo(new ThumbAlign(1));
        assertThat(ThumbAlign.parse("focus:1x1")).isEqualTo(new ThumbAlign(1f, 1f));
        assertThat(ThumbAlign.parse("focus:1.2x1.2")).isEqualTo(new ThumbAlign(1.2f, 1.2f));
    }

    @Test
    public void shouldFormat() throws Exception {
        assertThat(new ThumbAlign(ThumbAlignType.LEFT).toString()).isEqualTo("left");
        assertThat(new ThumbAlign(ThumbAlignType.RIGHT).toString()).isEqualTo("right");
        assertThat(new ThumbAlign(ThumbAlignType.CENTER).toString()).isEqualTo("center");
        assertThat(new ThumbAlign(ThumbAlignType.TOP).toString()).isEqualTo("top");
        assertThat(new ThumbAlign(ThumbAlignType.BOTTOM).toString()).isEqualTo("bottom");
        assertThat(new ThumbAlign(ThumbAlignType.AUTO).toString()).isEqualTo("auto");
        assertThat(new ThumbAlign(0).toString()).isEqualTo("face:0");
        assertThat(new ThumbAlign(1).toString()).isEqualTo("face:1");
        assertThat(new ThumbAlign(1, 1).toString()).isEqualTo("focus:1.0x1.0");
    }
}