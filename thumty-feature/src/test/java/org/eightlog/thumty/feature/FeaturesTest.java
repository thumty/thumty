package org.eightlog.thumty.feature;

import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.image.geometry.Feature;
import org.eightlog.thumty.image.geometry.FeatureType;
import org.junit.Test;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FeaturesTest {

    @Test
    public void shouldScaleFeatures() throws Exception {
        List<Feature> features = Arrays.asList(
                new Feature(new Rectangle(0, 0, 10, 10), 1),
                new Feature(new Rectangle(10, 10, 10, 10), 1)
        );

        Features f = new Features(100, 100, features);

        assertThat(f.resize(101, 101).getWidth()).isEqualTo(101);
        assertThat(f.resize(101, 101).getHeight()).isEqualTo(101);
        assertThat(f.resize(101, 101).getFeatures().get(0)).isEqualTo(new Feature(new Rectangle(0, 0, 10, 10), 1));
        assertThat(f.resize(101, 101).getFeatures().get(1)).isEqualTo(new Feature(new Rectangle(10, 10, 10, 10), 1));

        assertThat(f.resize(50, 50).getWidth()).isEqualTo(50);
        assertThat(f.resize(50, 50).getHeight()).isEqualTo(50);
        assertThat(f.resize(50, 50).getFeatures().get(0)).isEqualTo(new Feature(new Rectangle(0, 0, 5, 5), 1));
        assertThat(f.resize(50, 50).getFeatures().get(1)).isEqualTo(new Feature(new Rectangle(5, 5, 5, 5), 1));
    }

    @Test
    public void shouldConvertToJson() throws Exception {
        List<Feature> features = Arrays.asList(
                new Feature(new Rectangle(0, 0, 10, 10), 1),
                new Feature(new Rectangle(10, 11, 12, 13), FeatureType.EYE, 3.141)
        );

        Features f = new Features(100, 101, features);

        JsonObject json = f.toJson();

        assertThat(json.getInteger("width")).isEqualTo(100);
        assertThat(json.getInteger("height")).isEqualTo(101);
        assertThat(json.getJsonArray("features").getList()).hasSize(2);
        assertThat(json.getJsonArray("features").getJsonObject(1).getInteger("x")).isEqualTo(10);
        assertThat(json.getJsonArray("features").getJsonObject(1).getInteger("y")).isEqualTo(11);
        assertThat(json.getJsonArray("features").getJsonObject(1).getInteger("width")).isEqualTo(12);
        assertThat(json.getJsonArray("features").getJsonObject(1).getInteger("height")).isEqualTo(13);
        assertThat(json.getJsonArray("features").getJsonObject(1).getDouble("weight")).isEqualTo(3.141);
        assertThat(json.getJsonArray("features").getJsonObject(1).getString("type")).isEqualTo(FeatureType.EYE.name());
    }

    @Test
    public void shouldConvertFromJson() throws Exception {
        Features f = new Features(new JsonObject("{\n" +
                "  \"width\": 100,\n" +
                "  \"height\": 101,\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"x\": 11,\n" +
                "      \"y\": 12,\n" +
                "      \"width\": 13,\n" +
                "      \"height\": 14,\n" +
                "      \"type\": \"FACE\",\n" +
                "      \"weight\": 3.141\n" +
                "    }\n" +
                "  ]\n" +
                "}"));

        assertThat(f).isEqualTo(new Features(100, 101, Collections.singletonList(new Feature(new Rectangle(11, 12, 13, 14), FeatureType.FACE, 3.141))));
    }
}