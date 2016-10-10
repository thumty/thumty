package org.eightlog.thumty.feature;

import org.fest.assertions.data.Index;
import org.junit.Test;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FeaturesTest {

    @Test
    public void shouldScaleFeatures() throws Exception {
        List<FeatureRegion> regions = Arrays.asList(
                new FeatureRegion(0, 0, 10, 10, 1),
                new FeatureRegion(10, 10, 10, 10, 1)
        );

        Features features = new Features(100, 100, regions);

        assertThat(features.resize(101, 101).getWidth()).isEqualTo(101);
        assertThat(features.resize(101, 101).getHeight()).isEqualTo(101);
        assertThat(features.resize(101, 101).getRegions().get(0)).isEqualTo(new FeatureRegion(0, 0, 10, 10, 1));
        assertThat(features.resize(101, 101).getRegions().get(1)).isEqualTo(new FeatureRegion(10, 10, 10, 10, 1));

        assertThat(features.resize(50, 50).getWidth()).isEqualTo(50);
        assertThat(features.resize(50, 50).getHeight()).isEqualTo(50);
        assertThat(features.resize(50, 50).getRegions().get(0)).isEqualTo(new FeatureRegion(0, 0, 5, 5, 1));
        assertThat(features.resize(50, 50).getRegions().get(1)).isEqualTo(new FeatureRegion(5, 5, 5, 5, 1));
    }

    @Test
    public void shouldComposeFeatures() throws Exception {
        List<FeatureRegion> regions = Arrays.asList(
                new FeatureRegion(0, 0, 10, 10, 1),
                new FeatureRegion(10, 10, 10, 10, 1)
        );

        Features a = new Features(100, 100, regions);
        Features b = new Features(200, 200, regions);

        assertThat(a.compose(b).getWidth()).isEqualTo(100);
        assertThat(a.compose(b).getHeight()).isEqualTo(100);
        assertThat(a.compose(b).getRegions())
                .hasSize(4)
                .contains(new FeatureRegion(0, 0, 10, 10, 1), Index.atIndex(0))
                .contains(new FeatureRegion(0, 0, 5, 5, 1), Index.atIndex(2))
                .contains(new FeatureRegion(5, 5, 5, 5, 1), Index.atIndex(3));
    }

    @Test
    public void shouldCrop() throws Exception {
        List<FeatureRegion> regions = Arrays.asList(
                new FeatureRegion(0, 0, 10, 10, 1),
                new FeatureRegion(10, 10, 10, 10, 1)
        );

        Features features = new Features(100, 100, regions);

        assertThat(features.crop(new Rectangle(10, 10, 10, 10)).getWidth()).isEqualTo(10);
        assertThat(features.crop(new Rectangle(10, 10, 10, 10)).getHeight()).isEqualTo(10);
        assertThat(features.crop(new Rectangle(10, 10, 10, 10)).getRegions())
                .contains(new FeatureRegion(0, 0, 0, 0, 1), Index.atIndex(0))
                .contains(new FeatureRegion(0, 0, 10, 10, 1), Index.atIndex(1));
    }

    @Test
    public void shouldGetNth() throws Exception {
        List<FeatureRegion> regions = Arrays.asList(
                new FeatureRegion(0, 0, 10, 10, 1),
                new FeatureRegion(10, 10, 10, 10, 1)
        );

        Features features = new Features(100, 100, regions);

        assertThat(features.nthRegion(0).getWidth()).isEqualTo(100);
        assertThat(features.nthRegion(0).getHeight()).isEqualTo(100);
        assertThat(features.nthRegion(0).getRegions())
                .hasSize(1)
                .contains(new FeatureRegion(0, 0, 10, 10, 1), Index.atIndex(0));
        assertThat(features.nthRegion(1).getRegions())
                .hasSize(1)
                .contains(new FeatureRegion(10, 10, 10, 10, 1), Index.atIndex(0));
        assertThat(features.nthRegion(2).getRegions())
                .hasSize(0);
    }
}