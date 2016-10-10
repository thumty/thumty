package org.eightlog.thumty.loader.http;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eightlog.thumty.cache.ContentCache;
import org.eightlog.thumty.store.Attributes;
import org.eightlog.thumty.store.ExpirableAttributedContent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class HttpLoaderTest {

    @Rule
    public WireMockRule wire = new WireMockRule();

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldLoadURL(TestContext ctx) throws Exception {
        Async async = ctx.async();

        wire.stubFor(get(urlEqualTo("/resource"))
                .willReturn(aResponse().withBody("content")));

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                ContentCache cache = new FakeContentCache();

                new HttpLoader(vertx.createHttpClient(), null, cache, 1000, 0, 0, 0).handler(res -> {
                    ctx.assertNotNull(res);
                    async.complete();
                }).exceptionHandler(t -> {
                    ctx.fail(t);
                    async.complete();
                }).load("http://localhost:8080/resource");
            }
        });
    }

    @Test
    public void shouldSetExpiration(TestContext ctx) throws Exception {
        Async async = ctx.async(4);

        LocalDateTime future = LocalDateTime.now().plusDays(100);
        LocalDateTime now = LocalDateTime.now();

        wire.stubFor(get(urlEqualTo("/has-header"))
                .willReturn(aResponse().withHeader("Expires", RFC_1123_DATE_TIME.format(future.atZone(ZoneId.systemDefault()))).withBody("content")));

        wire.stubFor(get(urlEqualTo("/expires-in-past"))
                .willReturn(aResponse().withHeader("Expires", RFC_1123_DATE_TIME.format(now.minusDays(1).atZone(ZoneId.systemDefault()))).withBody("content")));

        wire.stubFor(get(urlEqualTo("/has-cache-control"))
                .willReturn(aResponse().withHeader("Cache-Control", "max-age=60000").withBody("content")));

        wire.stubFor(get(urlEqualTo("/has-no-header"))
                .willReturn(aResponse().withBody("content")));

        FakeContentCache cache = new FakeContentCache();


        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {

                new HttpLoader(vertx.createHttpClient(),  null, cache, 1000, 0, 1000, 0).handler(res -> {
                    async.countDown();
                }).exceptionHandler(t -> {
                    ctx.fail(t);
                    async.countDown();
                }).load("http://localhost:8080/has-header");

                new HttpLoader(vertx.createHttpClient(),  null, cache, 1000, 0, 1000, 0).handler(res -> {
                    async.countDown();
                }).exceptionHandler(t -> {
                    ctx.fail(t);
                    async.countDown();
                }).load("http://localhost:8080/has-cache-control");

                new HttpLoader(vertx.createHttpClient(),  null, cache, 1000, 0, 1000, 0).handler(res -> {
                    async.countDown();
                }).exceptionHandler(t -> {
                    ctx.fail(t);
                    async.countDown();
                }).load("http://localhost:8080/has-no-header");

                new HttpLoader(vertx.createHttpClient(),  null, cache, 1000, 0, 1000, 0).handler(res -> {
                    async.countDown();
                }).exceptionHandler(t -> {
                    ctx.fail(t);
                    async.countDown();
                }).load("http://localhost:8080/expires-in-past");
            }
        });

        async.handler(res -> {
            ctx.assertTrue(
                    Duration.between(future, cache.expirations.get("http://localhost:8080/has-header")).getSeconds() < 1
            );

            ctx.assertTrue(
                    Duration.between(
                            cache.expirations.get("http://localhost:8080/has-cache-control"),
                            now.plusSeconds(60000)
                    ).getSeconds() < 1
            );

            ctx.assertTrue(
                    Duration.between(
                            cache.expirations.get("http://localhost:8080/has-no-header"),
                            now.plusSeconds(1)
                    ).getSeconds() < 1
            );

            ctx.assertTrue(
                    Duration.between(
                            cache.expirations.get("http://localhost:8080/expires-in-past"),
                            now.plusSeconds(1)
                    ).getSeconds() < 1
            );
        });
    }

    @Test
    public void shouldFollow301Redirects(TestContext ctx) throws Exception {
        Async async = ctx.async(2);

        LocalDateTime future = LocalDateTime.now().plusDays(100);

        wire.stubFor(get(urlEqualTo("/resource"))
                .willReturn(aResponse().withHeader("Expires", RFC_1123_DATE_TIME.format(future.atZone(ZoneId.systemDefault()))).withBody("content")));

        wire.stubFor(get(urlEqualTo("/redirect-301"))
                .willReturn(aResponse().withStatus(301).withHeader("Location", "http://localhost:8080/resource")));

        wire.stubFor(get(urlEqualTo("/redirect-relative-301"))
                .willReturn(aResponse().withStatus(301).withHeader("Location", "/resource")));

        FakeContentCache cache = new FakeContentCache();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {

                new HttpLoader(vertx.createHttpClient(), null, cache, 1000, 5, 1000, 0).handler(res -> {
                    async.countDown();
                }).exceptionHandler(t -> {
                    ctx.fail(t);
                    async.countDown();
                }).load("http://localhost:8080/redirect-relative-301");

                new HttpLoader(vertx.createHttpClient(), null, cache, 1000, 5, 1000, 0).handler(res -> {
                    async.countDown();
                }).exceptionHandler(t -> {
                    ctx.fail(t);
                    async.countDown();
                }).load("http://localhost:8080/redirect-301");
            }
        });

        async.handler(res -> {
            ctx.assertTrue(cache.keys.contains("http://localhost:8080/redirect-relative-301"));
            ctx.assertTrue(cache.keys.contains("http://localhost:8080/redirect-301"));
            ctx.assertTrue(cache.keys.contains("http://localhost:8080/resource"));

            ctx.assertTrue(
                    Duration.between(
                            future,
                            cache.expirations.get("http://localhost:8080/redirect-relative-301")
                    ).getSeconds() < 1
            );

            ctx.assertTrue(
                    Duration.between(
                            future,
                            cache.expirations.get("http://localhost:8080/redirect-301")
                    ).getSeconds() < 1
            );

            ctx.assertTrue(
                    Duration.between(
                            future,
                            cache.expirations.get("http://localhost:8080/resource")
                    ).getSeconds() < 1
            );
        });
    }

    @Test
    public void shouldFollowTemporalRedirects(TestContext ctx) throws Exception {
        int[] codes = new int[]{302, 303, 307};

        for (int code : codes) {
            Async async = ctx.async(2);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = LocalDateTime.now().plusDays(100);

            wire.stubFor(get(urlEqualTo("/resource"))
                    .willReturn(aResponse().withHeader("Expires", RFC_1123_DATE_TIME.format(future.atZone(ZoneId.systemDefault()))).withBody("content")));

            wire.stubFor(get(urlEqualTo("/redirect"))
                    .willReturn(aResponse().withStatus(code).withHeader("Location", "http://localhost:8080/resource")));

            wire.stubFor(get(urlEqualTo("/redirect-relative"))
                    .willReturn(aResponse().withStatus(code).withHeader("Location", "/resource")));

            FakeContentCache cache = new FakeContentCache();


            rule.vertx().deployVerticle(new AbstractVerticle() {
                @Override
                public void start() throws Exception {

                    new HttpLoader(vertx.createHttpClient(), null, cache, 1000, 5, 1000, 0).handler(res -> {
                        async.countDown();
                    }).exceptionHandler(t -> {
                        ctx.fail(t);
                        async.countDown();
                    }).load("http://localhost:8080/redirect-relative");

                    new HttpLoader(vertx.createHttpClient(), null, cache, 1000, 5, 1000, 0).handler(res -> {
                        async.countDown();
                    }).exceptionHandler(t -> {
                        ctx.fail(t);
                        async.countDown();
                    }).load("http://localhost:8080/redirect");
                }
            });

            async.handler(res -> {
                ctx.assertTrue(cache.keys.contains("http://localhost:8080/redirect-relative"));
                ctx.assertTrue(cache.keys.contains("http://localhost:8080/redirect"));
                ctx.assertTrue(cache.keys.contains("http://localhost:8080/resource"));

                ctx.assertTrue(
                        Duration.between(
                                now.plusSeconds(1),
                                cache.expirations.get("http://localhost:8080/redirect")
                        ).getSeconds() < 1
                );

                ctx.assertTrue(
                        Duration.between(
                                future,
                                cache.expirations.get("http://localhost:8080/resource")
                        ).getSeconds() < 1
                );
            });
        }
    }

    @Test
    public void shouldHandleMaxRedirects(TestContext ctx) throws Exception {
        Async async = ctx.async();

        wire.stubFor(get(urlEqualTo("/redirect"))
                .willReturn(aResponse().withStatus(301).withHeader("Location", "http://localhost:8080/redirect")));

        FakeContentCache cache = new FakeContentCache();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                new HttpLoader(vertx.createHttpClient(), null, cache, 1000, 5, 1000, 0).handler(res -> {
                    ctx.fail("Should not get content");
                    async.complete();
                }).exceptionHandler(t -> {
                    async.complete();
                }).load("http://localhost:8080/redirect");
            }
        });
    }

    @Test
    public void shouldHandleErrorResponse(TestContext ctx) throws Exception {
        Async async = ctx.async();

        wire.stubFor(get(urlEqualTo("/resource"))
                .willReturn(aResponse().withStatus(404)));

        FakeContentCache cache = new FakeContentCache();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                new HttpLoader(vertx.createHttpClient(), null, cache, 1000, 5, 1000, 0).handler(res -> {
                    ctx.fail("Should not get content");
                    async.complete();
                }).exceptionHandler(t -> {
                    async.complete();
                }).load("http://localhost:8080/resource");

            }
        });
    }

    private static class FakeContentCache implements ContentCache {
        private Set<String> keys = new HashSet<>();
        private Map<String, LocalDateTime> expirations = new HashMap<>();

        @Override
        public void getIfPresent(String key, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
            if (keys.contains(key)) {
                handler.handle(Future.succeededFuture(new FakeContentCacheRecord(expirations.get(key))));
            } else {
                handler.handle(Future.succeededFuture(null));
            }
        }

        @Override
        public void put(String key, ReadStream<Buffer> content, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
            put(key, content, null, handler);
        }

        @Override
        public void put(String key, ReadStream<Buffer> content, LocalDateTime expires, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
            keys.add(key);
            if (expires != null) {
                expirations.put(key, expires);
            } else {
                expirations.remove(key);
            }
            handler.handle(Future.succeededFuture(new FakeContentCacheRecord(expires)));
        }

        @Override
        public void put(String source, String target, LocalDateTime expires, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
            if (keys.contains(source)) {
                keys.add(target);
                if (expires != null) {
                    expirations.put(target, expires);
                } else {
                    expirations.remove(target);
                }
                handler.handle(Future.succeededFuture(new FakeContentCacheRecord(expires)));
            } else {
                handler.handle(Future.failedFuture("not found"));
            }
        }

        @Override
        public void invalidate(String key, Handler<AsyncResult<Void>> handler) {

        }

        @Override
        public void invalidateAll(Iterable<String> keys, Handler<AsyncResult<Void>> handler) {

        }

        @Override
        public void cleanUp(Handler<AsyncResult<Void>> handler) {

        }
    }

    private static class FakeContentCacheRecord implements ExpirableAttributedContent {

        private LocalDateTime expires;

        public FakeContentCacheRecord(LocalDateTime expires) {
            this.expires = expires;
        }

        @Override
        public LocalDateTime getExpires() {
            return expires;
        }

        @Override
        public Attributes getAttributes() {
            return null;
        }

        @Override
        public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
            return this;
        }

        @Override
        public ReadStream<Buffer> handler(Handler<Buffer> handler) {
            return this;
        }

        @Override
        public ReadStream<Buffer> pause() {
            return this;
        }

        @Override
        public ReadStream<Buffer> resume() {
            return this;
        }

        @Override
        public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
            return this;
        }
    }
}