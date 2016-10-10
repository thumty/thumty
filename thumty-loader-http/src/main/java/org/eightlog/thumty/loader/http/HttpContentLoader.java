package org.eightlog.thumty.loader.http;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.cache.ContentCache;
import org.eightlog.thumty.common.text.Wildcard;
import org.eightlog.thumty.loader.ContentLoader;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class HttpContentLoader implements ContentLoader {

    private final Vertx vertx;

    private final ContentCache contentCache;

    private final HttpContentLoaderOptions options;

    public HttpContentLoader(Vertx vertx, ContentCache contentCache, JsonObject config) {
        this.vertx = vertx;
        this.contentCache = contentCache;
        this.options =  new HttpContentLoaderOptions(config);
    }

    @Override
    public boolean canLoadResource(String resource) {
        try {
            URI location = URI.create(resource);

            if (location.getScheme() == null
                    || location.getScheme().equalsIgnoreCase("http")
                    || location.getScheme().equalsIgnoreCase("https")) {
                return !getPermittedAbsoluteUris(location).isEmpty();
            }
        }catch (IllegalArgumentException ignore) {
            // Ignore
        }

        return false;
    }

    @Override
    public void load(String resource, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        try{
            URI location = URI.create(resource);

            load(getPermittedAbsoluteUris(location), handler);
        }catch (IllegalArgumentException e) {
            handler.handle(Future.failedFuture(e));
        }
    }

    private void load(List<URI> alternatives, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        if (alternatives.isEmpty()) {
            throw new IllegalArgumentException("Empty alternative uris");
        }

        URI location = alternatives.get(0);

        if (alternatives.size() == 1) {
            load(location, handler);
        } else {
            load(location, res -> {
                if (res.succeeded()) {
                    handler.handle(Future.succeededFuture(res.result()));
                } else {
                    load(alternatives.subList(1, alternatives.size()), handler);
                }
            });
        }
    }

    private void load(URI resource, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        HttpClient client = vertx.createHttpClient(getClientOptions());
        HttpClient sslClient = vertx.createHttpClient(getSslClientOptions());

        new HttpLoader(client, sslClient, contentCache, options.getRequestTimeout(), options.getMaxRedirects(), options.getMinCacheTime(), options.getMaxCacheTime())
                .handler(content -> {
                    handler.handle(Future.succeededFuture(content));

                    client.close();
                    sslClient.close();
                })
                .exceptionHandler(err -> {
                    handler.handle(Future.failedFuture(err));

                    client.close();
                    sslClient.close();
                })
                .load(resource.toString());
    }

    private HttpClientOptions getClientOptions() {
        HttpClientOptions clientOptions = new HttpClientOptions();

        clientOptions.setConnectTimeout(options.getConnectionTimeout());
        clientOptions.setIdleTimeout(options.getConnectionIdleTimeout());
        clientOptions.setMaxPoolSize(options.getConnectionPoolSize());

        return clientOptions;
    }

    private HttpClientOptions getSslClientOptions() {
        return new HttpClientOptions(getClientOptions()).setSsl(true).setTrustAll(true);
    }

    private List<URI> getAbsoluteUris(URI resource) {
        if (options.getBaseUris().isEmpty()) {
            if (resource.getScheme() == null) {
                try {
                    return Arrays.asList(
                            URI.create("http://" + resource),
                            URI.create("https://" + resource)
                    );
                } catch (IllegalArgumentException ignore) {
                    // Ignore
                }
            } else {
                return Collections.singletonList(resource);
            }
        }

        return options.getBaseUris().stream().map(uri -> uri.resolve(resource)).collect(Collectors.toList());
    }

    private List<URI> getPermittedAbsoluteUris(URI resource) {
        return getAbsoluteUris(resource).stream().filter(this::isPermitted).collect(Collectors.toList());
    }

    private boolean isPermitted(URI uri) {
        for (Wildcard wildcard : options.getHostsAllowed()) {
            if (wildcard.matches(uri.getHost())) {
                return true;
            }
        }

        for (Wildcard wildcard : options.getHostsRejected()) {
            if (wildcard.matches(uri.getHost())) {
                return false;
            }
        }

        return options.getHostsAllowed().isEmpty();
    }

}
