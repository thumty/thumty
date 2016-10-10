package org.eightlog.thumty.image.geometry;

import org.junit.Test;

import static org.eightlog.thumty.image.ImageAssertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class RelativeOrAbsoluteSizeTest {

    @Test
    public void shouldCalculateSize() throws Exception {
        Size size;

        size = new RelativeOrAbsoluteSize(.5, .5);

        assertThat(size.calculate(10, 10).width).isEqualTo(5);
        assertThat(size.calculate(10, 10).height).isEqualTo(5);

        size = new RelativeOrAbsoluteSize(.5, 1);

        assertThat(size.calculate(10, 10).width).isEqualTo(5);
        assertThat(size.calculate(10, 10).height).isEqualTo(1);

        size = new RelativeOrAbsoluteSize(.5, 0);

        assertThat(size.calculate(10, 10).width).isEqualTo(5);
        assertThat(size.calculate(10, 10).height).isEqualTo(10);

        size = new RelativeOrAbsoluteSize(10, 10);

        assertThat(size.calculate(10, 10).width).isEqualTo(10);
        assertThat(size.calculate(10, 10).height).isEqualTo(10);

        size = new RelativeOrAbsoluteSize(0, 0);

        assertThat(size.calculate(10, 10).width).isEqualTo(10);
        assertThat(size.calculate(10, 10).height).isEqualTo(10);
    }
}