package org.eightlog.thumty.feature;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.feature.converter.FeatureConverter;
import org.eightlog.thumty.image.geometry.Feature;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@DataObject
public class Features implements Serializable {

    private final int width;

    private final int height;

    private final List<Feature> features;

    public Features(JsonObject json) {
        this.width = json.getInteger("width", 0);
        this.height = json.getInteger("height", 0);
        this.features = new ArrayList<>();

        if (json.getJsonArray("features") != null) {
            json.getJsonArray("features").forEach(item -> {
                if (item instanceof JsonObject) {
                    features.add(FeatureConverter.fromJson((JsonObject) item));
                }
            });
        }
    }

    public Features() {
        this(1, 1, Collections.emptyList());
    }

    public Features(BufferedImage image, List<Feature> features) {
        this(image.getWidth(), image.getHeight(), features);
    }

    public Features(int width, int height, List<Feature> features) {
        this.width = width;
        this.height = height;
        this.features = features;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("width", width);
        json.put("height", height);
        json.put("features", (JsonArray) features.stream()
                .map(FeatureConverter::toJson).collect(JsonArray::new, JsonArray::<JsonObject>add, JsonArray::addAll));
        return json;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public Features resize(int width, int height) {
        List<Feature> sizedRegions = new ArrayList<>();

        double scaleX = (double) width / this.width;
        double scaleY = (double) height / this.height;

        for (Feature feature : features) {
            sizedRegions.add(feature.scale(scaleX, scaleY));
        }

        return new Features(width, height, sizedRegions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Features)) return false;
        Features features1 = (Features) o;
        return width == features1.width &&
                height == features1.height &&
                Objects.equals(features, features1.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, features);
    }
}
