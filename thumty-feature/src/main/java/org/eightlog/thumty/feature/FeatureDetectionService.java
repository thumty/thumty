package org.eightlog.thumty.feature;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@ProxyGen
@VertxGen
public interface FeatureDetectionService {

    String FEATURE_DETECTOR_ADDRESS = "org.eightlog.thumty.feature.detector";

    static FeatureDetectionService create(Vertx vertx, JsonObject config) {
        return new FeatureDetectionServiceImpl(vertx, config);
    }

    static FeatureDetectionService createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(FeatureDetectionService.class, vertx, address);
    }

    /**
     * Search any feature in resource
     *
     * @param resource the resource location
     * @param handler  the result handler
     */
    void detectAny(String resource, Handler<AsyncResult<Features>> handler);

    /**
     * Search all features in resource
     * @param resource the resource location
     * @param handler the result handler
     */
    void detectAll(String resource, Handler<AsyncResult<Features>> handler);

    void detectFace(String resource, Handler<AsyncResult<Features>> handler);
}
