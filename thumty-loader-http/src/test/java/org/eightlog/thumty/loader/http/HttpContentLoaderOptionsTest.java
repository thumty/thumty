package org.eightlog.thumty.loader.http;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class HttpContentLoaderOptionsTest {

    @Test
    public void shouldParseOptions() throws Exception {
        HttpContentLoaderOptions options = new HttpContentLoaderOptions(new JsonObject("{\n" +
                "  \"connection_timeout\": \"120sec\",\n" +
                "  \"connection_idle_timeout\": \"80sec\",\n" +
                "  \"connection_pool_size\": 10,\n" +
                "  \"request_timeout\": \"130sec\",\n" +
                "  \"max_redirects\": 5,\n" +
                "  \"min_cache_time\": \"2h\",\n" +
                "  \"max_cache_time\": \"24h\"\n" +
                "}"));

        assertThat(options.getConnectionTimeout()).isEqualTo(120000);
        assertThat(options.getConnectionIdleTimeout()).isEqualTo(80000);
        assertThat(options.getConnectionPoolSize()).isEqualTo(10);
        assertThat(options.getRequestTimeout()).isEqualTo(130000);
        assertThat(options.getMaxRedirects()).isEqualTo(5);
        assertThat(options.getMinCacheTime()).isEqualTo(2 * 60 * 60 * 1000);
        assertThat(options.getMaxCacheTime()).isEqualTo(24 * 60 * 60 * 1000);
    }

    @Test
    public void shouldParseBaseUrl() throws Exception {
        HttpContentLoaderOptions options = new HttpContentLoaderOptions(new JsonObject(
                        "{\n" +
                        "  \"base_url\": \"http://example.com/path\"\n" +
                        "}"));

        assertThat(options.getBaseUris()).isEqualTo(Collections.singletonList(URI.create("http://example.com/path")));
    }

    @Test
    public void shouldParseBaseUris() throws Exception {
        HttpContentLoaderOptions options = new HttpContentLoaderOptions(new JsonObject(
                        "{\n" +
                        "  \"base_url\": [\"http://example.com/a\", \"http://example.com/b\", \"invalid\", \"unknown:/::\"] \n" +
                        "}"));

        assertThat(options.getBaseUris()).isEqualTo(Arrays.asList(URI.create("http://example.com/a"), URI.create("http://example.com/b")));
    }

    @Test
    public void shouldParseAllowedHosts() throws Exception {
        HttpContentLoaderOptions options = new HttpContentLoaderOptions(new JsonObject(
                        "{\n" +
                        "  \"hosts\":  {\n" +
                        "    \"allow\" : [\"*.example.com\", \"example.com\"]\n" +
                        "  }\n" +
                        "}"));

        assertThat(options.getHostsAllowed().get(0).matches("www.example.com"));
        assertThat(options.getHostsAllowed().get(1).matches("example.com"));
    }

    @Test
    public void shouldParseRejectedHosts() throws Exception {
        HttpContentLoaderOptions options = new HttpContentLoaderOptions(new JsonObject(
                "{\n" +
                        "  \"hosts\":  {\n" +
                        "    \"deny\" : [\"*.example.com\", \"example.com\"]\n" +
                        "  }\n" +
                        "}"));

        assertThat(options.getHostsRejected().get(0).matches("www.example.com"));
        assertThat(options.getHostsRejected().get(1).matches("example.com"));
    }
}