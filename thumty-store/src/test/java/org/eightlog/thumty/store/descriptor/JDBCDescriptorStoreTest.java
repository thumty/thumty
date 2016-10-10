package org.eightlog.thumty.store.descriptor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.eightlog.thumty.store.NotFoundException;
import org.eightlog.thumty.store.descriptor.jdbc.JDBCDescriptorStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class JDBCDescriptorStoreTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldWrite(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize(client -> {
            JDBCDescriptorStore store = new JDBCDescriptorStore(client, "descriptors");
            store.create("id", new Descriptor("sha1", 12345, 1, 12345, "content/type"), r0 -> {
                store.read("id", r1 -> {
                    ctx.assertEquals(r1.result().getSha1(), "sha1");
                    ctx.assertEquals(r1.result().getNumberOfChunks(), 1L);
                    ctx.assertEquals(r1.result().getChunkSize(), 12345L);
                    ctx.assertEquals(r1.result().getSize(), 12345L);
                    ctx.assertEquals(r1.result().getContentType(), "content/type");
                    async.complete();
                });

            });
        });
    }

    @Test
    public void shouldIncreaseReferenceCountOnCreate(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize(client -> {
            JDBCDescriptorStore store = new JDBCDescriptorStore(client, "descriptors");

            store.create("duplicate", mock(Descriptor.class), r0 -> {
                ctx.assertEquals(1L, r0.result());

                store.create("duplicate", mock(Descriptor.class), r1 -> {
                    ctx.assertEquals(2L, r1.result());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void shouldDecreaseReferenceCountOnDelete(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize(client -> {
            JDBCDescriptorStore store = new JDBCDescriptorStore(client, "descriptors");

            store.create("duplicate", mock(Descriptor.class), r0 -> {
                ctx.assertEquals(1L, r0.result());

                store.delete("duplicate", r1 -> {
                    ctx.assertEquals(0L, r1.result());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void shouldRead(TestContext ctx) throws Exception {
        Async read = ctx.async();
        Async error = ctx.async();

        initialize(client -> {
            JDBCDescriptorStore store = new JDBCDescriptorStore(client, "descriptors");

            store.create("id", mock(Descriptor.class), r0 -> {

                store.read("id", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertNotNull(r1.result());
                    read.complete();
                });

                store.read("unknown id", r2 -> {
                    ctx.assertFalse(r2.succeeded());
                    ctx.assertEquals(r2.cause().getClass(), NotFoundException.class);
                    error.complete();
                });
            });
        });
    }

    @Test
    public void shouldCheckExistence(TestContext ctx) throws Exception {
        Async exists = ctx.async();
        Async notExists = ctx.async();

        initialize(client -> {
            JDBCDescriptorStore store = new JDBCDescriptorStore(client, "descriptors");

            store.create("id", mock(Descriptor.class), r0 -> {

                store.exists("id", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertTrue(r1.result());
                    exists.complete();
                });

                store.exists("unknown id", r2 -> {
                    ctx.assertTrue(r2.succeeded());
                    ctx.assertFalse(r2.result());
                    notExists.complete();
                });
            });
        });
    }

    @Test
    public void shouldIncreaseReferenceCount(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize(client -> {
            JDBCDescriptorStore store = new JDBCDescriptorStore(client, "descriptors");

            store.create("id", mock(Descriptor.class), r0 -> {
                store.duplicate("id", r1 -> {
                    ctx.assertTrue(r1.succeeded());
                    ctx.assertEquals(2L, r1.result());

                    store.delete("id", r2 -> {
                        ctx.assertTrue(r2.succeeded());
                        ctx.assertEquals(1L, r2.result());
                        async.complete();
                    });
                });
            });
        });
    }

    private void initialize(Handler<JDBCClient> handler) {
        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                JDBCClient client = JDBCClient.createShared(vertx, new JsonObject()
                        .put("url", "jdbc:hsqldb:mem:db")
                        .put("driver_class", "org.hsqldb.jdbcDriver"));

                JDBCWrapper jdbc = JDBCWrapper.create(client);

                jdbc.update("DROP TABLE IF EXISTS descriptors")
                        .compose(v -> jdbc.update("CREATE TABLE IF NOT EXISTS descriptors (id VARCHAR(128) NOT NULL, " +
                                "sha1 VARCHAR(40), " +
                                "chunk_size BIGINT, " +
                                "number_of_chunks BIGINT, " +
                                "number_of_references BIGINT, " +
                                "size BIGINT, " +
                                "content_type VARCHAR(1024), " +
                                "PRIMARY KEY (id))"))
                        .map(client)
                        .setHandler(res -> {
                            if (res.succeeded()) {
                                handler.handle(client);
                            }
                        });
            }
        });
    }
}