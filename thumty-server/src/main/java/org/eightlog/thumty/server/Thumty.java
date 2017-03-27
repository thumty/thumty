package org.eightlog.thumty.server;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.eightlog.thumty.feature.FeatureDetectionVertical;
import org.eightlog.thumty.image.io.UnsupportedFormatException;
import org.eightlog.thumty.loader.LoaderException;
import org.eightlog.thumty.server.params.ThumbParams;
import org.eightlog.thumty.server.params.ThumbParamsParser;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Thumty extends AbstractVerticle {

    private final static Logger LOGGER = LoggerFactory.getLogger(Thumty.class);

    private static final Pattern VARIANT_PATH_PATTERN = Pattern.compile("^/([^/]+)/(.*)");

    private static final String CONTEXT_THUMB_PARAMS = "__thumty.ThumbParams";

    private ThumbBuilder thumbBuilder;

    private ThumtyOptions options;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        thumbBuilder = ThumbBuilder.builder(vertx);
        options = new ThumtyOptions(config());

        DeploymentOptions featureDetectionDeploymentOptions = new DeploymentOptions().setConfig(config());

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

                router.route().handler(LoggerHandler.create());
                router.route().handler(ResponseTimeHandler.create());

                if (options.getResponseTimeout() > 0) {
                    router.route().handler(TimeoutHandler.create(options.getResponseTimeout()));
                }

                router.get().handler(this::buildVariant);
                router.get().handler(this::buildThumb);

                vertx.createHttpServer().requestHandler(router::accept)
                        .listen(options.getPort(), options.getHost(), onServerStart);
            } else {
                startFuture.fail(res.cause());
            }
        });
    }

    private void buildVariant(RoutingContext context) {
        HttpServerRequest request = context.request();

        request.path();

        Matcher matcher = VARIANT_PATH_PATTERN.matcher(request.path());

        if (matcher.matches()) {
            String variant = matcher.group(1);
            String source = matcher.group(2);

            ThumbParams params = options.getVariant(variant);

            if (params != null) {
                params.setSource(source);
                params.setSigned(true);
                context.put(CONTEXT_THUMB_PARAMS, params);
            }
        }

        context.next();
    }

    private void buildThumb(RoutingContext context) {
        HttpServerRequest request = context.request();

        ThumbParams params = context.get(CONTEXT_THUMB_PARAMS);

        if (params == null) {
            params = ThumbParamsParser.parse(options.getSecret(), request.path());
        }

        if (options.isSecured() && !params.isSigned()) {
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
        LocalDateTime now = LocalDateTime.now();
        if (time.isBefore(now)) {
            return "no-cache";
        } else {
            return "max-age=" + Duration.between(now, time).getSeconds();
        }
    }
}
