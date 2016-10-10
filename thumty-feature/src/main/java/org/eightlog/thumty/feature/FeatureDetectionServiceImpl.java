package org.eightlog.thumty.feature;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import org.eightlog.thumty.cache.Cache;
import org.eightlog.thumty.cache.CacheManager;
import org.eightlog.thumty.common.stream.ReadStreamInputStream;
import org.eightlog.thumty.feature.detector.CommonFeatureDetector;
import org.eightlog.thumty.feature.detector.FeatureDetector;
import org.eightlog.thumty.feature.detector.FrontFaceDetector;
import org.eightlog.thumty.feature.detector.ProfileFaceDetector;
import org.eightlog.thumty.image.io.Image;
import org.eightlog.thumty.image.io.InputStreamImageInput;
import org.eightlog.thumty.image.io.UnsupportedFormatException;
import org.eightlog.thumty.image.io.sampler.SizeSampler;
import org.eightlog.thumty.loader.Loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FeatureDetectionServiceImpl implements FeatureDetectionService {

    /**
     * Features cache name
     */
    private final static String FEATURE_CACHE_NAME = "features";

    /**
     * Read stream timeout
     */
    private final static long READ_STREAM_TIMEOUT = 100;

    private final Vertx vertx;

    private final Loaders loaders;

    private final Cache<Features> cache;

    private final FeatureDetectionServiceOptions options;

    private FeatureDetector faceDetector;

    private FeatureDetector anyDetector;

    private FeatureDetector allDetector;

    public FeatureDetectionServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.loaders = Loaders.createShared(vertx);
        this.options = new FeatureDetectionServiceOptions(config);
        this.cache = CacheManager.createShared(vertx).getCache(FEATURE_CACHE_NAME, options.getCacheConfig());

        FrontFaceDetector frontFaceDetector = new FrontFaceDetector(options.getFrontFaceWeight());
        ProfileFaceDetector profileFaceDetector = new ProfileFaceDetector(options.getProfileFaceWeight());
        CommonFeatureDetector commonFeatureDetector = new CommonFeatureDetector(options.getFeatureWeight());

        this.faceDetector = FeatureDetector.all(frontFaceDetector, profileFaceDetector);
        this.anyDetector = FeatureDetector.any(frontFaceDetector, profileFaceDetector, commonFeatureDetector);
        this.allDetector = FeatureDetector.all(frontFaceDetector, profileFaceDetector, commonFeatureDetector);
    }

    @Override
    public void detectAny(String resource, Handler<AsyncResult<Features>> handler) {
        detect(resource, "any", anyDetector).setHandler(handler);
    }

    @Override
    public void detectAll(String resource, Handler<AsyncResult<Features>> handler) {
        detect(resource, "all", allDetector).setHandler(handler);
    }

    @Override
    public void detectFace(String resource, Handler<AsyncResult<Features>> handler) {
        detect(resource, "face", faceDetector).setHandler(handler);
    }

    private Future<Features> detect(String resource, String detectorId, FeatureDetector detector) {
        String cacheKey = detectorId + "/" + resource;

        return cache.getIfPresent(cacheKey).compose(features -> {
            if (features == null) {
                return detectUncached(resource, detectorId, detector);
            } else {
                return Future.succeededFuture(features);
            }
        });
    }

    private Future<Features> detectUncached(String resource, String detectorId, FeatureDetector detector) {
        String cacheKey = detectorId + "/" + resource;

        return loaders.getLoader(resource)
                .load(resource)
                .compose(content ->
                        detect(content, detector)
                                .compose(features -> cache.put(cacheKey, features, content.getExpires()).map(features))
                );
    }

    private Future<Features> detect(ReadStream<Buffer> stream, FeatureDetector detector) {
        Future<Features> future = Future.future();
        vertx.executeBlocking(result -> {
            try {
                try (InputStream input = new ReadStreamInputStream(stream, READ_STREAM_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    Image source = new InputStreamImageInput(new SizeSampler(options.getResize())).read(input);
                    result.complete(detector.detect(source.getBufferedImage()));
                } catch (IOException | UnsupportedFormatException e) {
                    result.fail(e);
                }
            } catch (Throwable t) {
                result.fail(t);
            }
        }, false, future.completer());
        return future;
    }
}
