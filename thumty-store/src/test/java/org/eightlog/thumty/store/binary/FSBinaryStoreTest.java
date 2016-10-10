package org.eightlog.thumty.store.binary;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eightlog.thumty.store.NotFoundException;
import org.eightlog.thumty.store.descriptor.Descriptor;
import org.eightlog.thumty.store.descriptor.DescriptorStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class FSBinaryStoreTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldWriteContentToStore(TestContext ctx) throws Exception {
        Async async = ctx.async();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                FSBinaryStore store = new FSBinaryStore(vertx, new FakeDescriptorStore());
                vertx.fileSystem().open("./src/test/resources/sample.jpg", new OpenOptions().setRead(true), f -> {
                    store.create(f.result(), res -> {
                        ctx.assertTrue(res.succeeded());
                        ctx.assertNotNull(res.result());
                        async.complete();
                    });
                });
            }
        });
    }

    @Test
    public void shouldReadStoredContent(TestContext ctx) throws Exception {
        Async async = ctx.async();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                FSBinaryStore store = new FSBinaryStore(vertx, new FakeDescriptorStore());
                vertx.fileSystem().open("./src/test/resources/sample.jpg", new OpenOptions().setRead(true), f -> {
                    store.create(f.result()).compose(i -> store.read(i.getId())).setHandler(res -> {
                        ctx.assertTrue(res.succeeded());
                        async.complete();
                    });
                });
            }
        });
    }

    @Test
    public void shouldThrowOnMissingContent(TestContext ctx) throws Exception {
        Async async = ctx.async();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                FSBinaryStore store = new FSBinaryStore(vertx, new FakeDescriptorStore());
                store.read("fake id").setHandler(res -> {
                    ctx.assertFalse(res.succeeded());
                    ctx.assertEquals(res.cause().getClass(), NotFoundException.class);
                    async.complete();
                });
            }
        });
    }

    @Test
    public void shouldCheckExistence(TestContext ctx) throws Exception {
        Async async = ctx.async();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                FSBinaryStore store = new FSBinaryStore(vertx, new FakeDescriptorStore());
                vertx.fileSystem().open("./src/test/resources/sample.jpg", new OpenOptions().setRead(true), f -> {
                    store.create(f.result()).compose(i -> store.exists(i.getId())).setHandler(res -> {
                        ctx.assertTrue(res.succeeded());
                        ctx.assertTrue(res.result());
                        async.complete();
                    });
                });
            }
        });
    }

    @Test
    public void shouldReadInfo(TestContext ctx) throws Exception {
        Async async = ctx.async();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                FSBinaryStore store = new FSBinaryStore(vertx, new FakeDescriptorStore());
                vertx.fileSystem().open("./src/test/resources/sample.jpg", new OpenOptions().setRead(true), f -> {
                    store.create(f.result()).compose(i -> store.get(i.getId())).setHandler(res -> {
                        ctx.assertTrue(res.succeeded());
                        ctx.assertEquals(res.result().getAttributes().getContentType(), "image/jpeg");
                        ctx.assertEquals(res.result().getAttributes().getSize(), 73544L);
                        ctx.assertEquals(res.result().getAttributes().getSha1(), "ccafa6d826621e7b6836d6276a0f274c674c086b");
                        async.complete();
                    });
                });
            }
        });
    }

    @Test
    public void shouldDuplicateContent(TestContext ctx) throws Exception {
        Async async = ctx.async();

        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                FSBinaryStore store = new FSBinaryStore(vertx, new FakeDescriptorStore());
                vertx.fileSystem().open("./src/test/resources/sample.jpg", new OpenOptions().setRead(true), f -> {
                    store.create(f.result()).compose(i -> store.get(i.getId())).setHandler(r0 -> {
                        String id = r0.result().getAttributes().getSha1();

                        store.duplicate(id, r1 -> {
                            ctx.assertTrue(r1.succeeded());

                            store.delete(id, r2 -> {
                                ctx.assertTrue(r2.succeeded());

                                store.exists(id, r3 -> {
                                    ctx.assertTrue(r3.succeeded());
                                    ctx.assertTrue(r3.result());

                                    async.complete();
                                });
                            });
                        });
                        async.complete();
                    });
                });
            }
        });


    }

    /**
     * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
     */
    private static class FakeDescriptorStore implements DescriptorStore {

        private Map<String, Descriptor> store = new HashMap<>();
        private Map<String, Long> refCounter = new HashMap<>();

        @Override
        public void create(String id, Descriptor descriptor, Handler<AsyncResult<Long>> handler) {
            store.put(id, descriptor);
            handler.handle(Future.succeededFuture(refCounter.compute(id, (key, count) -> count != null ? count++ : 1)));
        }

        @Override
        public void read(String id, Handler<AsyncResult<Descriptor>> handler) {
            Descriptor descriptor = store.get(id);

            if (descriptor != null) {
                handler.handle(Future.succeededFuture(descriptor));
            } else {
                handler.handle(Future.failedFuture("Not found"));
            }
        }

        @Override
        public void exists(String id, Handler<AsyncResult<Boolean>> handler) {
            handler.handle(Future.succeededFuture(store.containsKey(id)));
        }

        @Override
        public void delete(String id, Handler<AsyncResult<Long>> handler) {
            Long count = refCounter.get(id);
            if (count == null) {
                handler.handle(Future.succeededFuture(-1L));
            } else {
                count = count - 1;
                if (count == 0) {
                    store.remove(id);
                }
                handler.handle(Future.succeededFuture(count));
            }
        }

        @Override
        public void duplicate(String id, Handler<AsyncResult<Long>> handler) {
            refCounter.compute(id, (key, count) -> count != null ? count++ : null);
        }
    }
}