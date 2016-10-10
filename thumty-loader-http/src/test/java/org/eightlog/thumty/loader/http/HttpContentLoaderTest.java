package org.eightlog.thumty.loader.http;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.cache.ContentCache;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class HttpContentLoaderTest {

    @Test
    public void shouldConfigureDefaults() throws Exception {
        HttpContentLoader loader = new HttpContentLoader(mock(Vertx.class), mock(ContentCache.class), new JsonObject());

        assertThat(loader.canLoadResource("http://example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("https://example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("image.jpg")).isTrue();
        assertThat(loader.canLoadResource("fs://image.jpg")).isFalse();
        assertThat(loader.canLoadResource("ftp://image.jpg")).isFalse();
    }

    @Test
    public void shouldConfigureAllowedHosts() throws Exception {
        HttpContentLoader loader = new HttpContentLoader(mock(Vertx.class), mock(ContentCache.class),
                new JsonObject("{\n" +
                        "  \"hosts\": {\n" +
                        "    \"allow\": \"*.example.com\"\n" +
                        "  }\n" +
                        "}"));

        assertThat(loader.canLoadResource("http://example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("https://example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("http://www.example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("https://www.example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("www.example.com/image.jpg")).isTrue();
    }

    @Test
    public void shouldConfigureMultiplyAllowedHosts() throws Exception {
        HttpContentLoader loader = new HttpContentLoader(mock(Vertx.class), mock(ContentCache.class),
                new JsonObject("{\n" +
                        "  \"hosts\": {\n" +
                        "    \"allow\": [\"*.example.com\", \"example.com\"]\n" +
                        "  }\n" +
                        "}"));

        assertThat(loader.canLoadResource("http://example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("https://example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("http://www.example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("https://www.example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("www.example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("www.unknown.com/image.jpg")).isFalse();
    }

    @Test
    public void shouldConfigureRestrictedHosts() throws Exception {
        HttpContentLoader loader = new HttpContentLoader(mock(Vertx.class), mock(ContentCache.class),
                new JsonObject("{\n" +
                        "  \"hosts\": {\n" +
                        "    \"deny\": \"*.example.com\"\n" +
                        "  }\n" +
                        "}"));

        assertThat(loader.canLoadResource("http://example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("https://example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("http://www.example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("https://www.example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("example.com/image.jpg")).isTrue();
        assertThat(loader.canLoadResource("www.example.com/image.jpg")).isFalse();
    }

    @Test
    public void shouldConfigureMultiplyRestrictedHosts() throws Exception {
        HttpContentLoader loader = new HttpContentLoader(mock(Vertx.class), mock(ContentCache.class),
                new JsonObject("{\n" +
                        "  \"hosts\": {\n" +
                        "    \"deny\": [\"*.example.com\", \"example.com\"]\n" +
                        "  }\n" +
                        "}"));

        assertThat(loader.canLoadResource("http://example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("https://example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("http://www.example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("https://www.example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("www.example.com/image.jpg")).isFalse();
        assertThat(loader.canLoadResource("www.unknown.com/image.jpg")).isTrue();
    }

}