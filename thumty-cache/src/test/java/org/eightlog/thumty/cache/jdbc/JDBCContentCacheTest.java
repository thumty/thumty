package org.eightlog.thumty.cache.jdbc;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.eightlog.thumty.store.content.Content;
import org.eightlog.thumty.store.content.ContentAttributes;
import org.eightlog.thumty.store.content.ContentStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class JDBCContentCacheTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldPut(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            FakeContentStore store = new FakeContentStore();
            JDBCContentCache cache = new JDBCContentCache(store, client, "cache");

            cache.put("key", mock(ReadStream.class), r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("key", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNotNull(r1.result());

                    ctx.assertNotNull(store.content.get("key"));

                    async.complete();
                });
            });
        });
    }

    @Test
    public void shouldCopy(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            FakeContentStore store = new FakeContentStore();
            JDBCContentCache cache = new JDBCContentCache(store, client, "cache");

            cache.put("key", mock(ReadStream.class), r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.put("key", "copy", LocalDateTime.now().plusDays(1), r1 -> {
                    ctx.assertTrue(r1.succeeded());

                    cache.getIfPresent("copy", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNotNull(r2.result());

                        ctx.assertNotNull(store.content.get("copy"));

                        async.complete();
                    });
                });
            });
        });
    }

    @Test
    public void shouldInvalidateKey(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            FakeContentStore contentStore = new FakeContentStore();
            JDBCContentCache cache = new JDBCContentCache(contentStore, client, "cache");

            cache.put("key", mock(ReadStream.class), r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.invalidate("key", r1 -> {
                    ctx.assertTrue(r1.succeeded());

                    cache.getIfPresent("key", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNull(r2.result());

                        // Check content store
                        ctx.assertFalse(contentStore.content.containsKey("key"));

                        async.complete();
                    });
                });
            });
        });
    }

    @Test
    public void shouldInvalidateAllKeys(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            FakeContentStore store = new FakeContentStore();
            JDBCContentCache cache = new JDBCContentCache(store, client, "cache");

            List<String> keys = new ArrayList<>();
            List<Future> futures = new ArrayList<>();

            for (int i = 0; i < 1000; i++) {
                keys.add("key" + i);
                futures.add(cache.put("key" + i, mock(ReadStream.class)));
            }

            CompositeFuture.join(futures).setHandler(r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.invalidateAll(keys, r1 -> {
                    ctx.assertTrue(r1.succeeded());

                    cache.getIfPresent("key555" , r2 -> {
                        ctx.assertTrue(r1.succeeded());
                        ctx.assertNull(r1.result());

                        // Check content store
                        ctx.assertTrue(store.content.isEmpty());

                        async.complete();
                    });
                });
            });
        });
    }

    @Test
    public void shouldCleanUpExpired(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            JDBCWrapper jdbc = JDBCWrapper.create(client);

            FakeContentStore store = new FakeContentStore();
            JDBCContentCache cache = new JDBCContentCache(store, client, "cache");

            store.content.put("a1", mock(ReadStream.class));
            store.content.put("a2", mock(ReadStream.class));
            store.content.put("a3", mock(ReadStream.class));

            store.content.put("b1", mock(ReadStream.class));
            store.content.put("b2", mock(ReadStream.class));
            store.content.put("b3", mock(ReadStream.class));

            jdbc.begin(tx ->
                    tx.batch(Arrays.asList(
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a1', 10, DATEADD('DAY', -1, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a2', 10, DATEADD('DAY', -2, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a3', 10, DATEADD('DAY', -3, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b1', 10, DATEADD('DAY', 1, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b2', 10, DATEADD('DAY', 2, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b3', 10, DATEADD('DAY', 3, NOW()), NOW())"
                    )).compose(tx::commit)
            ).compose(v-> cache.cleanUp()).setHandler(r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("a1", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNull(r1.result());

                    cache.getIfPresent("b1", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNotNull(r2.result());

                        ctx.assertNull(store.content.get("a1"));
                        ctx.assertNull(store.content.get("a2"));
                        ctx.assertNull(store.content.get("a3"));

                        ctx.assertNotNull(store.content.get("b1"));
                        ctx.assertNotNull(store.content.get("b2"));
                        ctx.assertNotNull(store.content.get("b3"));

                        async.complete();
                    });
                });
            });
        });
    }

    @Test
    public void shouldCleanUpSize(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            JDBCWrapper jdbc = JDBCWrapper.create(client);

            FakeContentStore store = new FakeContentStore();
            JDBCContentCache cache = new JDBCContentCache(store, client, "cache", 30, 0, 0, 1000);

            store.content.put("a1", mock(ReadStream.class));
            store.content.put("a2", mock(ReadStream.class));
            store.content.put("a3", mock(ReadStream.class));

            store.content.put("b1", mock(ReadStream.class));
            store.content.put("b2", mock(ReadStream.class));
            store.content.put("b3", mock(ReadStream.class));

            jdbc.begin(tx ->
                    tx.batch(Arrays.asList(
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a1', 10, DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -5, NOW()))",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a2', 10, DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -5, NOW()))",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a3', 10, DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -5, NOW()))",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b1', 10, DATEADD('DAY', 7, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b2', 10, DATEADD('DAY', 7, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b3', 10, DATEADD('DAY', 7, NOW()), NOW())"
                    )).compose(tx::commit)
            ).compose(v-> cache.cleanUp()).setHandler(r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("a1", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNull(r1.result());

                    cache.getIfPresent("b1", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNotNull(r2.result());

                        ctx.assertNull(store.content.get("a1"));
                        ctx.assertNull(store.content.get("a2"));
                        ctx.assertNull(store.content.get("a3"));

                        ctx.assertNotNull(store.content.get("b1"));
                        ctx.assertNotNull(store.content.get("b2"));
                        ctx.assertNotNull(store.content.get("b3"));

                        async.complete();
                    });
                });
            });
        });
    }

    @Test
    public void shouldCleanUpAccessed(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            JDBCWrapper jdbc = JDBCWrapper.create(client);

            FakeContentStore store = new FakeContentStore();
            JDBCContentCache cache = new JDBCContentCache(store, client, "cache", 0, TimeUnit.MINUTES.toMillis(5), 0, 1000);

            store.content.put("a1", mock(ReadStream.class));
            store.content.put("a2", mock(ReadStream.class));
            store.content.put("a3", mock(ReadStream.class));

            store.content.put("b1", mock(ReadStream.class));
            store.content.put("b2", mock(ReadStream.class));
            store.content.put("b3", mock(ReadStream.class));

            jdbc.begin(tx ->
                    tx.batch(Arrays.asList(
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a1', 10, DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -6, NOW()))",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a2', 10, DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -6, NOW()))",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('a3', 10, DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -6, NOW()))",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b1', 10, DATEADD('DAY', 7, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b2', 10, DATEADD('DAY', 7, NOW()), NOW())",
                            "INSERT INTO cache (key, size, expires, accessed) VALUES ('b3', 10, DATEADD('DAY', 7, NOW()), NOW())"
                    )).compose(tx::commit)
            ).compose(v-> cache.cleanUp()).setHandler(r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("a1", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNull(r1.result());

                    cache.getIfPresent("b1", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNotNull(r2.result());

                        ctx.assertNull(store.content.get("a1"));
                        ctx.assertNull(store.content.get("a2"));
                        ctx.assertNull(store.content.get("a3"));

                        ctx.assertNotNull(store.content.get("b1"));
                        ctx.assertNotNull(store.content.get("b2"));
                        ctx.assertNotNull(store.content.get("b3"));

                        async.complete();
                    });
                });
            });
        });
    }

    private void initialize(BiConsumer<Vertx, JDBCClient> handler) {
        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                JDBCClient client = JDBCClient.createShared(vertx, new JsonObject()
                        .put("url", "jdbc:hsqldb:mem:db")
                        .put("driver_class", "org.hsqldb.jdbcDriver"));

                JDBCWrapper jdbc = JDBCWrapper.create(client);

                jdbc.update("DROP TABLE IF EXISTS cache")
                        .compose(v -> jdbc.execute("CREATE TABLE IF NOT EXISTS cache (key VARCHAR(8192) NOT NULL, " +
                                "size BIGINT, " +
                                "expires TIMESTAMP, " +
                                "accessed TIMESTAMP," +
                                "PRIMARY KEY (key))"))
                        .map(client)
                        .setHandler(res -> {
                            if (res.succeeded()) {
                                handler.accept(vertx, res.result());
                            }
                        });
            }
        });
    }

    private static class FakeContentStore implements ContentStore {
        private Map<String, ReadStream<Buffer>> content = new HashMap<>();

        @Override
        public void write(String path, @Nullable JsonObject meta, ReadStream<Buffer> stream, Handler<AsyncResult<Content>> handler) {
            content.put(path, stream);
            handler.handle(Future.succeededFuture(new FakeContent(path, 1024 * 1024)));
        }

        @Override
        public void read(String path, Handler<AsyncResult<Content>> handler) {
            if(content.containsKey(path)) {
                handler.handle(Future.succeededFuture(new FakeContent(path, 1024 * 1024)));
            } else {
                handler.handle(Future.failedFuture("Not found"));
            }
        }

        @Override
        public void exists(String path, Handler<AsyncResult<Boolean>> handler) {
            if(content.containsKey(path)) {
                handler.handle(Future.succeededFuture(true));
            } else {
                handler.handle(Future.succeededFuture(false));
            }
        }

        @Override
        public void delete(String path, Handler<AsyncResult<Void>> handler) {
            content.remove(path);
            handler.handle(Future.succeededFuture());
        }

        @Override
        public void copy(String from, String to, Handler<AsyncResult<Content>> handler) {
            content.put(to, content.get(from));
            handler.handle(Future.succeededFuture(new FakeContent(to, 1024 * 1024)));
        }

        @Override
        public void copy(String from, String to, JsonObject meta, Handler<AsyncResult<Content>> handler) {
            content.put(to, content.get(from));
            handler.handle(Future.succeededFuture(new FakeContent(to, 1024 * 1024)));
        }

        @Override
        public void attributes(String path, Handler<AsyncResult<ContentAttributes>> handler) {
            if(content.containsKey(path)) {
                handler.handle(Future.succeededFuture(new FakeContent(path, 1024 * 1024).getAttributes()));
            } else {
                handler.handle(Future.failedFuture("Not found"));
            }
        }
    }

    private static class FakeContent implements Content {
        private final String path;

        private final long size;

        public FakeContent(String path, long size) {
            this.path = path;
            this.size = size;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public ContentAttributes getAttributes() {
            return new ContentAttributes("sha1", size, "content/type", LocalDateTime.now(), LocalDateTime.now(), new JsonObject());
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