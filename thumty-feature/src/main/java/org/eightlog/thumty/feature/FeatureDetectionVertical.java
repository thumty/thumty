package org.eightlog.thumty.feature;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FeatureDetectionVertical extends AbstractVerticle {

    private MessageConsumer<JsonObject> service;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        JsonObject config = config().getJsonObject("features", new JsonObject());

        service = ProxyHelper.registerService(FeatureDetectionService.class, vertx,
                FeatureDetectionService.create(vertx, config), FeatureDetectionService.FEATURE_DETECTOR_ADDRESS);

        service.completionHandler(startFuture.completer());
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        service.unregister(stopFuture.completer());
    }
}
