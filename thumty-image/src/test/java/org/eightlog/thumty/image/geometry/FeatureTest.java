package org.eightlog.thumty.image.geometry;

import org.junit.Test;

import java.awt.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FeatureTest {

    @Test
    public void shouldScaleRegion() throws Exception {
        assertThat(new Feature(new Rectangle(10, 20, 100, 100), FeatureType.COMMON, 1).scale(2, 3))
                .isEqualTo(new Feature(new Rectangle(20, 60, 200, 300), 1));
    }

    @Test
    public void shouldCrop() throws Exception {
        assertThat(new Feature(new Rectangle(10, 20, 100, 100), 1).crop(new Rectangle(12, 15, 90, 110)))
                .isEqualTo(new Feature(new Rectangle(0, 5, 90, 100), 1));

        assertThat(new Feature(new Rectangle(10, 20, 100, 100), 1).crop(new Rectangle(10, 15, 90, 110)))
                .isEqualTo(new Feature(new Rectangle(0, 5, 90, 100), 1));

        assertThat(new Feature(new Rectangle(0, 0, 100, 100), 1).crop(new Rectangle(10, 10, 90, 90)))
                .isEqualTo(new Feature(new Rectangle(0, 0, 90, 90), 1));

        assertThat(new Feature(new Rectangle(20, 0, 100, 100), 1).crop(new Rectangle(10, 10, 90, 90)))
                .isEqualTo(new Feature(new Rectangle(10, 0, 80, 90), 1));
    }

    @Test
    public void shouldCompare() throws Exception {
        assertThat(new Feature(new Rectangle(0, 0, 10, 10), 1).compareTo(new Feature(new Rectangle(1, 0, 10, 10), 1))).isLessThan(0);
        assertThat(new Feature(new Rectangle(1, 0, 10, 10), 1).compareTo(new Feature(new Rectangle(0, 0, 10, 10), 1))).isGreaterThan(0);
        assertThat(new Feature(new Rectangle(1, 0, 10, 10), 1).compareTo(new Feature(new Rectangle(1, 1, 10, 10), 1))).isLessThan(0);
        assertThat(new Feature(new Rectangle(1, 1, 10, 10), 1).compareTo(new Feature(new Rectangle(1, 0, 10, 10), 1))).isGreaterThan(0);
        assertThat(new Feature(new Rectangle(1, 1, 10, 10), 1).compareTo(new Feature(new Rectangle(1, 1, 10, 10), 1))).isEqualTo(0);
    }
}