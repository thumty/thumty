package org.eightlog.thumty.feature.converter;

import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.image.geometry.Feature;
import org.eightlog.thumty.image.geometry.FeatureType;
import org.junit.Test;

import java.awt.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FeatureConverterTest {

    @Test
    public void shouldConvertFromJson() throws Exception {
        Feature feature = FeatureConverter.fromJson(new JsonObject("{\n" +
                "  \"x\": 11,\n" +
                "  \"y\": 12,\n" +
                "  \"width\": 13,\n" +
                "  \"height\": 14,\n" +
                "  \"type\": \"FACE\",\n" +
                "  \"weight\": 3.141\n" +
                "}"));

        assertThat(feature).isEqualTo(new Feature(new Rectangle(11, 12, 13, 14), FeatureType.FACE, 3.141));
    }

    @Test
    public void shouldConvertToJson() throws Exception {
        JsonObject json = FeatureConverter.toJson(
                new Feature(new Rectangle(11, 12, 13, 14), FeatureType.FACE, 3.141)
        );

        assertThat(json.getInteger("x")).isEqualTo(11);
        assertThat(json.getInteger("y")).isEqualTo(12);
        assertThat(json.getInteger("width")).isEqualTo(13);
        assertThat(json.getInteger("height")).isEqualTo(14);
        assertThat(json.getDouble("weight")).isEqualTo(3.141);
        assertThat(json.getString("type")).isEqualTo(FeatureType.FACE.name());
    }
}