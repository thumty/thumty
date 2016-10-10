package org.eightlog.thumty.loader.http;

import com.google.common.base.Splitter;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import org.eightlog.thumty.cache.ContentCache;
import org.eightlog.thumty.loader.LoaderException;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class HttpLoader {

    private final static String DEFAULT_ACCEPT = "*/*";
    private final static String DEFAULT_USER_AGENT = "thumty/1.0";

    private final HttpClient client;
    private final HttpClient sslClient;

    private final ContentCache cache;

    private final int requestTimeout;
    private final int redirects;

    private final long minCacheTime;
    private final long maxCacheTime;

    private Handler<ExpirableAttributedContent> handler;
    private Handler<Throwable> exceptionHandler;

    private boolean failed;

    public HttpLoader(HttpClient client, HttpClient sslClient, ContentCache cache,
                      int requestTimeout, int redirects, long minCacheTime, long maxCacheTime) {
        this.client = client;
        this.sslClient = sslClient;

        this.cache = cache;

        this.requestTimeout = requestTimeout;
        this.redirects = redirects;

        this.minCacheTime = minCacheTime;
        this.maxCacheTime = maxCacheTime;
    }

    public HttpLoader handler(Handler<ExpirableAttributedContent> handler) {
        this.handler = handler;
        return this;
    }

    public HttpLoader exceptionHandler(Handler<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public HttpLoader load(String url) {
        Objects.requireNonNull(url, "no null url accepted");

        cache.getIfPresent(url, res -> {
            if (res.succeeded()) {
                if (res.result() != null) {
                    handle(res.result());
                } else {
                    doLoad(url);
                }
            } else {
                fail(res.cause());
            }
        });

        return this;
    }

    private void doLoad(String resource) {
        try {
            URL url = new URL(resource);

            HttpClient protocolClient;

            if (url.getProtocol().equalsIgnoreCase("http")) {
                protocolClient = client;
            } else {
                protocolClient = sslClient;
            }

            protocolClient.requestAbs(HttpMethod.GET, resource, response -> readResponse(resource, response))
                    .putHeader("Accept", DEFAULT_ACCEPT)
                    .putHeader("User-Agent", DEFAULT_USER_AGENT)
                    .setTimeout(requestTimeout)
                    .exceptionHandler(this::fail)
                    .end();

        } catch (MalformedURLException e) {
            fail(new LoaderException(e));
        }
    }

    private void readResponse(String url, HttpClientResponse response) {
        switch (response.statusCode()) {
            case 200:
                readSuccessResponse(url, response);
                break;
            case 301:
                readPermanentRedirect(url, response);
                break;
            case 302:
            case 303:
            case 307:
                readTemporalRedirect(url, response);
                break;
            default:
                fail(new LoaderException("Invalid response status code: " + response.statusCode() + " " + response.statusMessage() + " for url " + url));

        }
    }

    private void readTemporalRedirect(String url, HttpClientResponse response) {
        if (redirects == 0) {
            fail(new LoaderException("Reached max redirects for " + url));
        }

        try {
            String location = URI.create(url).resolve(response.getHeader("Location")).toString();

            new HttpLoader(client, sslClient, cache, requestTimeout, redirects - 1, minCacheTime, maxCacheTime)
                    .handler(content -> {
                        cache.put(location, url, getDefaultExpires(), res -> {
                            if (res.succeeded()) {
                                handle(res.result());
                            } else {
                                fail(res.cause());
                            }
                        });
                    })
                    .exceptionHandler(this::fail)
                    .load(location);
        } catch (IllegalArgumentException | NullPointerException e) {
            fail(e);
        }
    }

    private void readPermanentRedirect(String url, HttpClientResponse response) {
        if (redirects == 0) {
            fail(new LoaderException("Reached max redirects for " + url));
        }

        try {
            String location = URI.create(url).resolve(response.getHeader("Location")).toString();

            new HttpLoader(client, sslClient, cache, requestTimeout, redirects - 1, minCacheTime, maxCacheTime)
                    .handler(content -> {
                        cache.put(location, url, content.getExpires(), res -> {
                            if (res.succeeded()) {
                                handle(res.result());
                            } else {
                                fail(res.cause());
                            }
                        });
                    })
                    .exceptionHandler(this::fail)
                    .load(location);
        } catch (IllegalArgumentException | NullPointerException e) {
            fail(e);
        }
    }

    private void readSuccessResponse(String url, HttpClientResponse response) {
        cache.put(url, response, getExpiration(response), res -> {
            if (res.succeeded()) {
                handle(res.result());
            } else {
                fail(res.cause());
            }
        });
    }

    private LocalDateTime getDefaultExpires() {
        LocalDateTime now = LocalDateTime.now();
        return minCacheTime > 0 ? now.plus(minCacheTime, ChronoUnit.MILLIS) : now;
    }

    private LocalDateTime getExpiration(final HttpClientResponse response) {
        final MultiMap headers = response.headers();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime min = minCacheTime > 0 ? now.plus(minCacheTime, ChronoUnit.MILLIS) : now;
        LocalDateTime max = maxCacheTime > 0 ? now.plus(maxCacheTime, ChronoUnit.MILLIS) : null;

        LocalDateTime expiration = null;

        if (headers.contains("Expires")) {
            try {
                expiration = LocalDateTime.parse(headers.get("Expires"), DateTimeFormatter.RFC_1123_DATE_TIME);
            } catch (DateTimeParseException ignore) {
                // Ignore
            }
        } else if (headers.contains("Cache-Control")) {
            try {
                final List<String> params = Splitter.on(",").trimResults().splitToList(headers.get("Cache-Control"));

                for (String param : params) {
                    if (param.startsWith("max-age=")) {
                        long seconds = Long.valueOf(param.substring("max-age=".length()));
                        expiration = now.plusSeconds(seconds);
                    }
                }

            } catch (NumberFormatException ignore) {
                // Ignore
            }
        }

        if (expiration != null) {
            if (expiration.isBefore(min)) {
                expiration = min;
            }

            if (max != null && expiration.isAfter(max)) {
                expiration = max;
            }
        } else {
            expiration = min;
        }

        return expiration;
    }

    private void handle(ExpirableAttributedContent record) {
        if (handler != null) {
            handler.handle(record);
        }
    }

    private synchronized void fail(Throwable throwable) {
        if (exceptionHandler != null && !failed) {
            failed = true;
            exceptionHandler.handle(throwable);
        }
    }

}
