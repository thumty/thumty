package org.eightlog.thumty.feature;

import com.google.common.collect.ImmutableList;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@DataObject(generateConverter = true)
public class Features implements Serializable {

    private int width;

    private int height;

    private List<FeatureRegion> regions;

    public Features(JsonObject json) {
        FeaturesConverter.fromJson(json, this);
    }

    public Features(BufferedImage image, List<FeatureRegion> regions) {
        this(image.getWidth(), image.getHeight(), regions);
    }

    public Features(int width, int height, List<FeatureRegion> regions) {
        this.width = width;
        this.height = height;
        this.regions = regions;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        FeaturesConverter.toJson(this, json);
        return json;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<FeatureRegion> getRegions() {
        return regions;
    }

    public void setRegions(List<FeatureRegion> regions) {
        this.regions = regions;
    }

    public Features resize(int width, int height) {
        List<FeatureRegion> sizedRegions = new ArrayList<>();

        double scaleX = (double)width / this.width;
        double scaleY = (double)height / this.height;

        for (FeatureRegion region : regions) {
            sizedRegions.add(region.scale(scaleX, scaleY));
        }

        return new Features(width, height, sizedRegions);
    }

    public boolean isEmpty() {
        return regions.isEmpty();
    }

    public Features compose(Features features) {
        if (isEmpty()) {
            return features;
        }
        return new Features(width, height,
                ImmutableList.<FeatureRegion>builder().addAll(getRegions()).addAll(features.resize(width, height).getRegions()).build());
    }

    public Features crop(Rectangle rectangle) {
        List<FeatureRegion> cropedRegions = new ArrayList<>();

        for (FeatureRegion region : regions) {
            cropedRegions.add(region.crop(rectangle));
        }

        return new Features(rectangle.width, rectangle.height, cropedRegions);
    }

    public Features nthRegion(int index) {
        FeatureRegion region = regions.stream().sorted().skip(index).findFirst().orElse(null);
        return new Features(width, height, region != null ? Collections.singletonList(region) : Collections.emptyList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Features)) return false;
        Features features = (Features) o;
        return width == features.width &&
                height == features.height &&
                Objects.equals(regions, features.regions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, regions);
    }

    @Override
    public String toString() {
        return "Features{" +
                "width=" + width +
                ", height=" + height +
                ", regions=" + regions +
                '}';
    }
}
