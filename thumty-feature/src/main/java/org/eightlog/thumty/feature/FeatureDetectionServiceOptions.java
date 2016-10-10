package org.eightlog.thumty.feature;

import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.image.geometry.RelativeOrAbsoluteSize;
import org.eightlog.thumty.image.geometry.Size;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FeatureDetectionServiceOptions {

    /**
     * Default common feature weight
     */
    private final static double DEFAULT_FEATURE_WEIGHT = 1;

    /**
     * Default front face feature weight
     */
    private final static double DEFAULT_FRONT_FACE_WEIGHT = DEFAULT_FEATURE_WEIGHT * 10;

    /**
     * Default profile face feature weight
     */
    private final static double DEFAULT_PROFILE_FACE_WEIGHT = DEFAULT_FEATURE_WEIGHT * 10;

    /**
     * Features cache configuration key
     */
    private final static String CACHE_CONFIG_KEY = "cache";


    private final JsonObject config;

    public FeatureDetectionServiceOptions(JsonObject config) {
        this.config = config;
    }

    public JsonObject getCacheConfig() {
        return config.getJsonObject(CACHE_CONFIG_KEY, new JsonObject());
    }

    public double getFeatureWeight() {
        return getWeightsConfig().getDouble("features", DEFAULT_FEATURE_WEIGHT);
    }

    public double getFrontFaceWeight() {
        return getWeightsConfig().getDouble("front_face", DEFAULT_FRONT_FACE_WEIGHT);
    }

    public double getProfileFaceWeight() {
        return getWeightsConfig().getDouble("profile_face", DEFAULT_PROFILE_FACE_WEIGHT);
    }

    public Size getResize() {
        return new RelativeOrAbsoluteSize(getWidth(), getHeight());
    }

    public int getWidth() {
        return getResizeConfig().getInteger("width", 0);
    }

    public int getHeight() {
        return getResizeConfig().getInteger("height", 0);
    }

    private JsonObject getResizeConfig() {
        return config.getJsonObject("resize", new JsonObject());
    }

    private JsonObject getWeightsConfig() {
        return config.getJsonObject("weights", new JsonObject());
    }
}
