package org.eightlog.thumty.loader.http;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.common.text.DurationParser;
import org.eightlog.thumty.common.text.Wildcard;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class HttpContentLoaderOptions {

    /**
     * Default connection timeout
     */
    private final static long DEFAULT_CONNECTION_TIMEOUT = 60_000;

    /**
     * Default request timeout
     */
    private final static int DEFAULT_REQUEST_TIMEOUT = 60_000;

    /**
     * Default pooled connection idle timeout
     */
    private final static int DEFAULT_IDLE_TIMEOUT = 60_000;

    /**
     * Default connection pool size
     */
    private final static int DEFAULT_POOL_SIZE = 10;

    /**
     * Default min cache time
     */
    private final static int DEFAULT_MIN_CACHE_TIME = 0;

    /**
     * Default max cache time
     */
    private final static int DEFAULT_MAX_CACHE_TIME = 0;

    /**
     * Default max redirects
     */
    private final static int DEFAULT_MAX_REDIRECTS = 4;

    private final JsonObject config;

    private List<Wildcard> hostsAllowed;

    private List<Wildcard> hostsRejected;

    private List<URI> baseUris;

    private Integer connectionTimeout;
    private Integer connectionIdleTimeout;
    private Integer requestTimeout;
    private Integer connectionPoolSize;
    private Integer maxRedirects;

    private Long minCacheTime;
    private Long maxCacheTime;

    public HttpContentLoaderOptions(JsonObject config) {
        this.config = config;
    }

    public int getConnectionTimeout() {
        if (connectionTimeout == null) {
            connectionTimeout = (int) DurationParser.parse(config.getString("connection_timeout"), DEFAULT_CONNECTION_TIMEOUT);
        }
        return connectionTimeout;
    }

    public int getConnectionIdleTimeout() {
        if (connectionIdleTimeout == null) {
            connectionIdleTimeout = (int) DurationParser.parse(config.getString("connection_idle_timeout"), DEFAULT_IDLE_TIMEOUT);
        }
        return connectionIdleTimeout;
    }

    public int getConnectionPoolSize() {
        if (connectionPoolSize == null) {
            connectionPoolSize = config.getInteger("connection_pool_size", DEFAULT_POOL_SIZE);
        }
        return connectionPoolSize;
    }

    public int getRequestTimeout() {
        if (requestTimeout == null) {
            requestTimeout = (int) DurationParser.parse(config.getString("request_timeout"), DEFAULT_REQUEST_TIMEOUT);
        }
        return requestTimeout;
    }

    public int getMaxRedirects() {
        if (maxRedirects == null) {
            maxRedirects = config.getInteger("max_redirects", DEFAULT_MAX_REDIRECTS);
        }
        return maxRedirects;
    }

    public long getMinCacheTime() {
        if (minCacheTime == null) {
            minCacheTime = DurationParser.parse(config.getString("min_cache_time"), DEFAULT_MIN_CACHE_TIME);
        }
        return minCacheTime;
    }

    public long getMaxCacheTime() {
        if (maxCacheTime == null) {
            maxCacheTime = DurationParser.parse(config.getString("max_cache_time"), DEFAULT_MAX_CACHE_TIME);
        }
        return maxCacheTime;
    }

    public List<URI> getBaseUris() {
        if (baseUris == null) {
            baseUris = new ArrayList<>();

            for (String url : read(config, "base_url")) {
                try{
                    URI uri = URI.create(url);
                    if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                        baseUris.add(uri);
                    }
                }catch (IllegalArgumentException ignore) {
                    // Ignore
                }
            }
        }
        return baseUris;
    }

    public List<Wildcard> getHostsAllowed() {
        if (hostsAllowed == null) {
            hostsAllowed = read(getHostsConfig(), "allow")
                    .stream()
                    .map(exp -> Wildcard.compile(exp, Pattern.CASE_INSENSITIVE))
                    .collect(Collectors.toList());
        }
        return hostsAllowed;
    }

    public List<Wildcard> getHostsRejected() {
        if (hostsRejected == null) {
            hostsRejected = read(getHostsConfig(), "deny")
                    .stream()
                    .map(exp -> Wildcard.compile(exp, Pattern.CASE_INSENSITIVE))
                    .collect(Collectors.toList());
        }
        return hostsRejected;
    }

    private JsonObject getHostsConfig() {
        return config.getJsonObject("hosts", new JsonObject());
    }

    private List<String> read(JsonObject config, String key) {
        Object value = config.getValue(key);

        if (value instanceof String) {
            return Collections.singletonList((String)value);
        }

        if (value instanceof JsonArray) {
            JsonArray values = (JsonArray) value;
            List<String> result = new ArrayList<>();

            for (int i = 0; i < values.size(); i++) {
                if (values.getString(i) != null) {
                    result.add(values.getString(i));
                }
            }

            return result;
        }

        return Collections.emptyList();
    }

}
