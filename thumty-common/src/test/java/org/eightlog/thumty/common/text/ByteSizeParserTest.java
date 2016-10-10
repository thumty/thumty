package org.eightlog.thumty.common.text;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ByteSizeParserTest {

    @Test
    public void shouldParseSize() throws Exception {
        assertThat(ByteSizeParser.parse("1")).isEqualTo(1);
        assertThat(ByteSizeParser.parse("1b")).isEqualTo(1);
        assertThat(ByteSizeParser.parse("1Kb")).isEqualTo(1000);
        assertThat(ByteSizeParser.parse("1.2Kb")).isEqualTo(1200);
        assertThat(ByteSizeParser.parse("1.3MB")).isEqualTo(13 * 100 * 1000L);
        assertThat(ByteSizeParser.parse("10Mb")).isEqualTo(10 * 1000 * 1000L);
        assertThat(ByteSizeParser.parse("1.532gB")).isEqualTo(1532 * 1000 * 1000L);

        assertThat(ByteSizeParser.parse("1kbb")).isEqualTo(0);
        assertThat(ByteSizeParser.parse("unknown")).isEqualTo(0);
    }
}