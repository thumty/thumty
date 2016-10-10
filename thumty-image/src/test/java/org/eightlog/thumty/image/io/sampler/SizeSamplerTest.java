package org.eightlog.thumty.image.io.sampler;

import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteSize;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class SizeSamplerTest {

    @Test
    public void shouldCalculateSampling() throws Exception {
        assertThat(new SizeSampler(new RelativeOrAbsoluteSize(100, 100)).getSampling(50, 50)).isEqualTo(1);
        assertThat(new SizeSampler(new RelativeOrAbsoluteSize(100, 100)).getSampling(100, 100)).isEqualTo(1);
        assertThat(new SizeSampler(new RelativeOrAbsoluteSize(100, 100)).getSampling(200, 200)).isEqualTo(2);
        assertThat(new SizeSampler(new RelativeOrAbsoluteSize(100, 100)).getSampling(300, 300)).isEqualTo(3);
        assertThat(new SizeSampler(new RelativeOrAbsoluteSize(100, 100)).getSampling(400, 400)).isEqualTo(4);
        assertThat(new SizeSampler(new RelativeOrAbsoluteSize(100, 100)).getSampling(200, 400)).isEqualTo(2);
    }
}