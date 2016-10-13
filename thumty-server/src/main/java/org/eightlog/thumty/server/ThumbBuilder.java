package org.eightlog.thumty.server;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import org.eightlog.thumty.cache.CacheManager;
import org.eightlog.thumty.cache.ContentCache;
import org.eightlog.thumty.common.stream.PipeStream;
import org.eightlog.thumty.common.stream.ReadStreamInputStream;
import org.eightlog.thumty.common.stream.WriteStreamOutputStream;
import org.eightlog.thumty.filter.common.AsyncThumbBuilder;
import org.eightlog.thumty.image.Image;
import org.eightlog.thumty.image.io.ImageOutput;
import org.eightlog.thumty.image.io.InputStreamImageInput;
import org.eightlog.thumty.image.io.OutputStreamImageOutput;
import org.eightlog.thumty.image.io.UnsupportedFormatException;
import org.eightlog.thumty.image.io.sampler.DefaultSampler;
import org.eightlog.thumty.image.io.sampler.ImageSampler;
import org.eightlog.thumty.image.io.sampler.X2Sampler;
import org.eightlog.thumty.loader.Loaders;
import org.eightlog.thumty.server.params.ThumbParams;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumbBuilder {

    /**
     * Thumb builder vertx config key
     */
    private final static String THUMB_CONFIG_KEY = "thumbs";

    /**
     * Thumb builder cache name
     */
    private final static String THUMB_CACHE_NAME = "thumb";

    /**
     * Default read stream timeout
     */
    private final static long READ_STREAM_TIMEOUT = 1000;

    /**
     * Default write stream timeout
     */
    private final static long WRITE_STREAM_TIMEOUT = 1000;

    private final Vertx vertx;

    private final ContentCache cache;

    private final Loaders loaders;

    private final ThumbBuilderOptions options;

    public ThumbBuilder(Vertx vertx) {
        this(vertx, getThumbBuilderConfig(vertx.getOrCreateContext().config()));
    }

    public ThumbBuilder(Vertx vertx, JsonObject config) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(config);

        this.vertx = vertx;
        this.loaders = Loaders.createShared(vertx);
        this.options = new ThumbBuilderOptions(config);
        this.cache = CacheManager.createShared(vertx)
                .getContentCache(THUMB_CACHE_NAME, options.getCacheConfig());
    }

    private static JsonObject getThumbBuilderConfig(JsonObject config) {
        return config != null ? config.getJsonObject(THUMB_CONFIG_KEY, new JsonObject()) : new JsonObject();
    }

    public static ThumbBuilder builder(Vertx vertx) {
        return new ThumbBuilder(vertx);
    }

    public Future<ExpirableAttributedContent> build(ThumbParams params) {
        Objects.requireNonNull(params);

        return getCached(params).compose(content -> {
            if (content == null) {
                return buildUncached(params);
            } else {
                return Future.succeededFuture(content);
            }
        });
    }

    private Future<ExpirableAttributedContent> buildUncached(ThumbParams params) {
        return loaders.getLoader(params.getSource()).load(params.getSource()).compose(content -> buildAndCache(content, params));
    }

    private Future<ExpirableAttributedContent> buildAndCache(ExpirableAttributedContent content, ThumbParams params) {
        return PipeStream.<ExpirableAttributedContent, Buffer>pipe(ws -> build(content, ws, params), rs -> cache(rs, params, content.getExpires()));
    }

    private Future<ExpirableAttributedContent> getCached(ThumbParams params) {
        return cache.getIfPresent(params.toString());
    }

    private Future<ExpirableAttributedContent> cache(ReadStream<Buffer> content, ThumbParams params, LocalDateTime expires) {
        return cache.put(params.toString(), content, expires);
    }

    private Future<Void> build(ReadStream<Buffer> input, WriteStream<Buffer> output, ThumbParams params) {
        return read(input, params)
                .compose(image -> new AsyncThumbBuilder(vertx, params).apply(image))
                .compose(image -> write(output, image));
    }

    /**
     * Write image to buffered stream
     *
     * @param stream the buffered write stream
     * @param image  the image
     * @return a future result
     */
    private Future<Void> write(WriteStream<Buffer> stream, Image image) {
        Future<Void> future = Future.future();

        vertx.executeBlocking(result -> {
            try (OutputStream output = new WriteStreamOutputStream(stream, WRITE_STREAM_TIMEOUT, TimeUnit.MILLISECONDS)) {

                ImageOutput writer = new OutputStreamImageOutput(output);

                float quality = getWriteQuality(image);
                String format = getWriteFormat(writer, image);

                new OutputStreamImageOutput(output).write(image.getSource(), format, quality);
                result.complete();
            } catch (IOException | UnsupportedFormatException e) {
                result.fail(e);
            }
        }, false, res -> {
            if (res.succeeded()) {
                future.complete();
            } else {
                future.fail(res.cause());
            }
        });
        return future;
    }

    /**
     * Read image from stream
     *
     * @param stream the buffered read stream
     * @param params the thumb params
     * @return a future image
     */
    private Future<Image> read(ReadStream<Buffer> stream, ThumbParams params) {
        Future<Image> future = Future.future();

        vertx.executeBlocking(result -> {
            try {
                try (InputStream input = new ReadStreamInputStream(stream, READ_STREAM_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    result.complete(new InputStreamImageInput(sampler(params)).read(input));
                } catch (IOException | UnsupportedFormatException e) {
                    result.fail(e);
                }
            } catch (Throwable t) {
                result.fail(t);
            }
        }, false, res -> {
            if (res.succeeded()) {
                future.complete((Image) res.result());
            } else {
                future.fail(res.cause());
            }
        });

        return future;
    }

    /**
     * Get image sampler for size
     *
     * @param params a thumb params
     * @return an image sampler
     */
    private ImageSampler sampler(ThumbParams params) {
        if (params.getSize() != null) {
            return new X2Sampler(params.getSize().toImageSize());
        }
        return new DefaultSampler();
    }

    private float getWriteQuality(Image image) {
        return Float.isNaN(image.getQuality()) ? options.getQuality() : image.getQuality();
    }

    private String getWriteFormat(ImageOutput writer, Image image) {
        String format = image.getFormat();

        if (!options.isSupportedFormat(format)) {
            format = getOutputFormat(image.getSource());
        }

        if (!writer.isSupportedFormat(format)) {
            format = getOutputFormat(image.getSource());
        }

        return format;
    }

    private String getOutputFormat(BufferedImage image) {
        switch (image.getType()) {
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "png";
        }
        return "jpg";
    }
}
