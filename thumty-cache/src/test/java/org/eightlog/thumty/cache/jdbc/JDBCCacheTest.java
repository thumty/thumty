package org.eightlog.thumty.cache.jdbc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class JDBCCacheTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldPut(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            JDBCCache<String> cache = new JDBCCache<>(client, "cache");

            cache.put("key", "data", r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("key", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertEquals("data", r1.result());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void shouldGetIfPresent(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, client) -> {
            JDBCCache<String> cache = new JDBCCache<>(client, "cache");

            cache.put("key", "data", r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("key", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNotNull(r1.result());

                    cache.getIfPresent("unknown", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNull(r2.result());

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
            JDBCCache<String> cache = new JDBCCache<>(client, "cache");

            cache.put("key", "data", r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.invalidate("key", r1 -> {
                    ctx.assertTrue(r1.succeeded());

                    cache.getIfPresent("key", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNull(r2.result());
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
            JDBCCache<String> cache = new JDBCCache<>(client, "cache");

            List<String> keys = new ArrayList<>();
            List<Future> futures = new ArrayList<>();

            for (int i = 0; i < 1000; i++) {
                keys.add("key" + i);
                futures.add(cache.put("key" + i, "data"));
            }

            CompositeFuture.join(futures).setHandler(r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.invalidateAll(keys, r1 -> {
                    ctx.assertTrue(r1.succeeded());

                    cache.getIfPresent("key555", r2 -> {
                        ctx.assertTrue(r1.succeeded());
                        ctx.assertNull(r1.result());

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

            JDBCCache<String> cache = new JDBCCache<>(client, "cache");

            jdbc.begin(tx ->
                    tx.batch(Arrays.asList(
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a1', X'ACED000574000464617461', DATEADD('DAY', -1, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a2', X'ACED000574000464617461', DATEADD('DAY', -2, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a3', X'ACED000574000464617461', DATEADD('DAY', -3, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b1', X'ACED000574000464617461', DATEADD('DAY', 1, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b2', X'ACED000574000464617461', DATEADD('DAY', 2, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b3', X'ACED000574000464617461', DATEADD('DAY', 3, NOW()), NOW())"
                    )).compose(tx::commit)
            ).compose(v -> cache.cleanUp()).setHandler(r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("a1", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNull(r1.result());

                    cache.getIfPresent("b1", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNotNull(r2.result());

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
            JDBCCache<String> cache = new JDBCCache<>(client, "cache", 3, 0, 0);

            jdbc.begin(tx ->
                    tx.batch(Arrays.asList(
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a1', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -5, NOW()))",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a2', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -5, NOW()))",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a3', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -5, NOW()))",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b1', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b2', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b3', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), NOW())"
                    )).compose(tx::commit)
            ).compose(v -> cache.cleanUp()).setHandler(r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("a1", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNull(r1.result());

                    cache.getIfPresent("b1", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNotNull(r2.result());

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
            JDBCCache<String> cache = new JDBCCache<>(client, "cache", 0, 0, TimeUnit.MINUTES.toMillis(5));

            jdbc.begin(tx ->
                    tx.batch(Arrays.asList(
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a1', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -6, NOW()))",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a2', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -6, NOW()))",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('a3', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), DATEADD('MINUTE', -6, NOW()))",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b1', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b2', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), NOW())",
                            "INSERT INTO cache (key, value, expires, accessed) VALUES ('b3', X'ACED000574000464617461', DATEADD('DAY', 7, NOW()), NOW())"
                    )).compose(tx::commit)
            ).compose(v -> cache.cleanUp()).setHandler(r0 -> {
                ctx.assertTrue(r0.succeeded());

                cache.getIfPresent("a1", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNull(r1.result());

                    cache.getIfPresent("b1", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertNotNull(r2.result());

                        async.complete();
                    });
                });
            });
        });
    }

    @Test
    public void shouldSetExpiration(TestContext ctx) throws Exception {
        Async async = ctx.async(2);

        initialize((vertx, client) -> {
            JDBCWrapper jdbc = JDBCWrapper.create(client);
            JDBCCache<String> cache = new JDBCCache<>(client, "cache", 0, TimeUnit.MINUTES.toMillis(5), 0);

            cache.put("key1", "data", r0 -> {
                ctx.assertTrue(r0.succeeded());

                jdbc.query("SELECT expires FROM cache WHERE key = ?", "key1").setHandler(r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertTrue(r1.result().getResults().size() > 0);
                    ctx.assertNotNull(r1.result().getResults().get(0).getInstant(0));
                    async.countDown();
                });
            });

            cache.put("key2", "data", LocalDateTime.now().plusMinutes(5), r0 -> {
                ctx.assertTrue(r0.succeeded());

                jdbc.query("SELECT expires FROM cache WHERE key = ?", "key2").setHandler(r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertTrue(r1.result().getResults().size() > 0);
                    ctx.assertNotNull(r1.result().getResults().get(0).getInstant(0));
                    async.countDown();
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
                                "value BLOB, " +
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

}