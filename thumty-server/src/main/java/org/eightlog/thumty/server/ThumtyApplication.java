package org.eightlog.thumty.server;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import org.eightlog.thumty.feature.FeatureDetectionVertical;
import org.eightlog.thumty.image.io.UnsupportedFormatException;
import org.eightlog.thumty.loader.LoaderException;
import org.eightlog.thumty.server.params.ThumbParams;
import org.eightlog.thumty.server.params.ThumbParamsParser;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumtyApplication extends AbstractVerticle {

    private final static Logger LOGGER = LoggerFactory.getLogger(ThumtyApplication.class);

    /**
     * Default http server host
     */
    private static final String DEFAULT_HOST = "0.0.0.0";

    /**
     * Default http server port
     */
    private static final int DEFAULT_PORT = 8080;

    private ThumbBuilder thumbBuilder;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        thumbBuilder = ThumbBuilder.builder(vertx);

        DeploymentOptions featureDetectionDeploymentOptions = new DeploymentOptions().setConfig(config());

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("PNG");
        while (readers.hasNext()) {
            System.out.println("reader: " + readers.next());
        }

        Handler<AsyncResult<HttpServer>> onServerStart = res -> {
            if (res.succeeded()) {
                LOGGER.debug("Http server started on port \"{0,number,#}\"", res.result().actualPort());
                startFuture.complete();
            } else {
                startFuture.fail(res.cause());
            }
        };

        vertx.deployVerticle(new FeatureDetectionVertical(), featureDetectionDeploymentOptions, res -> {
            if (res.succeeded()) {
                LOGGER.debug("Succeeded in deploying feature detection vertical");

                Router router = Router.router(vertx);

                router.get().handler(ResponseTimeHandler.create());
                router.get().handler(this::buildThumb);

                vertx.createHttpServer().requestHandler(router::accept)
                        .listen(
                                config().getInteger("port", DEFAULT_PORT),
                                config().getString("host", DEFAULT_HOST),
                                onServerStart
                        );
            } else {
                startFuture.fail(res.cause());
            }
        });
    }


    private void buildThumb(RoutingContext context) {
        HttpServerRequest request = context.request();

        ThumbParams params = ThumbParamsParser.parse(getSecret(), request.path());

        if (isSecured() && !params.isSigned()) {
            sendError(context, "Forbidden", 403);
        } else {
            LOGGER.debug("Processing request \"{0}\"", request.path());

            thumbBuilder.build(params).setHandler(res -> {
                if (res.succeeded()) {
                    LOGGER.debug("Succeeded in processing request \"{0}\"", request.path());
                    sendContent(context, res.result());
                } else {
                    LOGGER.error("Failed in processing request \"{0}\"", res.cause(), request.path());
                    sendError(context, res.cause());
                }
            });
        }
    }

    private void sendContent(RoutingContext context, ExpirableAttributedContent content) {
        context.response()
                .putHeader("Content-Type", content.getAttributes().getContentType())
                .putHeader("Content-Length", String.valueOf(content.getAttributes().getSize()));

        if (content.getExpires() != null) {
            context.response()
                    .putHeader("Expires", formatExpiration(content.getExpires()))
                    .putHeader("Cache-Control", formatCacheControl(content.getExpires()));
        }

        Pump.pump(content, context.response()).start();
        content.endHandler(v -> context.response().end());
    }

    private void sendError(RoutingContext context, Throwable throwable) {
        if (throwable instanceof UnsupportedFormatException) {
            sendError(context, "Unsupported image format", 422);
        } else if (throwable instanceof LoaderException) {
            sendError(context, "Not found", 404);
        } else {
            sendError(context, "Internal server error", 500);
        }
    }

    private void sendError(RoutingContext context, String message, int status) {
        context.response().setStatusCode(status).setStatusMessage(message).end();
    }

    private String formatExpiration(LocalDateTime time) {
        return RFC_1123_DATE_TIME.format(time.atZone(ZoneId.of("GMT")));
    }

    private String formatCacheControl(LocalDateTime time) {
        LocalDateTime now  = LocalDateTime.now();
        if (time.isBefore(now)) {
            return "no-cache";
        } else {
            return "max-age=" + Duration.between(now, time).getSeconds();
        }
    }

    private String getSecret() {
        return config().getString("secret", "");
    }

    private boolean isSecured() {
        return config().getBoolean("secured", false);
    }
}
