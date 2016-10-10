package org.eightlog.thumty.common.text;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class DurationParserTest {

    @Test
    public void shouldParseDuration() throws Exception {
        assertThat(DurationParser.parse("1ms")).isEqualTo(1);
        assertThat(DurationParser.parse("10ms")).isEqualTo(10);
        assertThat(DurationParser.parse("1000ms")).isEqualTo(1000);
        assertThat(DurationParser.parse("1s")).isEqualTo(1000);
        assertThat(DurationParser.parse("60s")).isEqualTo(60 * 1000);
        assertThat(DurationParser.parse("60s 1000ms")).isEqualTo(60 * 1000 + 1000);
        assertThat(DurationParser.parse("1m 60s 1000ms")).isEqualTo(120 * 1000 + 1000);
        assertThat(DurationParser.parse("2h 1m 60s 1000ms")).isEqualTo(TimeUnit.HOURS.toMillis(2) + 120 * 1000 + 1000);
        assertThat(DurationParser.parse("3d 2h 1m 60s 1000ms")).isEqualTo(TimeUnit.DAYS.toMillis(3) + TimeUnit.HOURS.toMillis(2) + 120 * 1000 + 1000);
        assertThat(DurationParser.parse("1w")).isEqualTo(TimeUnit.DAYS.toMillis(7));
    }

    @Test
    public void shouldParseLongDuration() throws Exception {
        assertThat(DurationParser.parse("3sec")).isEqualTo(3 * 1000);
        assertThat(DurationParser.parse("3second")).isEqualTo(3 * 1000);
        assertThat(DurationParser.parse("3seconds")).isEqualTo(3 * 1000);
        assertThat(DurationParser.parse("3min")).isEqualTo(3 * 60 * 1000);
        assertThat(DurationParser.parse("3minute")).isEqualTo(3 * 60 * 1000);
        assertThat(DurationParser.parse("3minutes")).isEqualTo(3 * 60 * 1000);
        assertThat(DurationParser.parse("3day")).isEqualTo(TimeUnit.DAYS.toMillis(3));
        assertThat(DurationParser.parse("3days")).isEqualTo(TimeUnit.DAYS.toMillis(3));
        assertThat(DurationParser.parse("3week")).isEqualTo(TimeUnit.DAYS.toMillis(3 * 7));
        assertThat(DurationParser.parse("3weeks")).isEqualTo(TimeUnit.DAYS.toMillis(3 * 7));
    }
}