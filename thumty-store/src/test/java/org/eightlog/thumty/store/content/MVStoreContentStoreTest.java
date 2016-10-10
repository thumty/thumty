package org.eightlog.thumty.store.content;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eightlog.thumty.store.Attributes;
import org.eightlog.thumty.store.NotFoundException;
import org.eightlog.thumty.store.binary.Binary;
import org.eightlog.thumty.store.binary.BinaryStore;
import org.eightlog.thumty.store.binary.ReadStreamBinary;
import org.h2.mvstore.MVStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class MVStoreContentStoreTest {
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldPersistFile(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, mvStore) -> {
            MVStoreContentStore store = new MVStoreContentStore(vertx, new FakeBinaryStore(), mvStore, "files");

            store.write("sample.jpg", new JsonObject().put("key", "value"), mock(ReadStream.class), res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                }
                ctx.assertTrue(res.succeeded());
                ctx.assertNotNull(res.result());

                store.attributes("sample.jpg", info -> {
                    ctx.assertTrue(info.succeeded());
                    ctx.assertEquals(info.result().getMeta().getString("key"), "value");
                });
                async.complete();
            });
        });
    }


    @Test
    public void shouldCreateCopy(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, mvStore) -> {
            MVStoreContentStore store = new MVStoreContentStore(vertx, new FakeBinaryStore(), mvStore, "files");

            store.write("sample.jpg", mock(ReadStream.class), v -> {

                store.copy("sample.jpg", "example.jpg", res -> {
                    ctx.assertTrue(res.succeeded());

                    store.attributes("example.jpg", info -> {
                        ctx.assertTrue(info.succeeded());

                        async.complete();
                    });
                });
            });
        });
    }

    @Test
    public void shouldCreateCopyAndOverrideMeta(TestContext ctx) throws Exception {
        Async async = ctx.async();

        initialize((vertx, mvStore) -> {
            MVStoreContentStore store = new MVStoreContentStore(vertx, new FakeBinaryStore(), mvStore, "files");

            store.write("sample.jpg", mock(ReadStream.class), v -> {
                store.copy("sample.jpg", "example.jpg", new JsonObject().put("key", "value"), res -> {

                    if (res.failed()) {
                        ctx.fail(res.cause());
                    }
                    ctx.assertTrue(res.succeeded());

                    store.attributes("example.jpg", info -> {
                        ctx.assertTrue(info.succeeded());
                        ctx.assertEquals(info.result().getMeta().getString("key"), "value");
                        async.complete();
                    });
                });
            });
        });
    }

    private void initialize(BiConsumer<Vertx, MVStore> handler) {
        rule.vertx().deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                handler.accept(vertx, new MVStore.Builder().open());
            }
        });

    }

    private static class FakeBinaryStore implements BinaryStore {

        private Map<String, ReadStream<Buffer>> streams = new HashMap<>();

        @Override
        public void create(ReadStream<Buffer> content, Handler<AsyncResult<Binary>> handler) {
            String id = UUID.randomUUID().toString();
            streams.put(id, content);

            handler.handle(Future.succeededFuture(new ReadStreamBinary(this, id, new Attributes(id, 0, ""))));
        }

        @Override
        public void duplicate(String id, Handler<AsyncResult<Binary>> handler) {
            handler.handle(Future.succeededFuture(new ReadStreamBinary(this, id, new Attributes(id, 0, ""))));
        }

        @Override
        public void read(String id, Handler<AsyncResult<ReadStream<Buffer>>> handler) {
            if (streams.containsKey(id)) {
                handler.handle(Future.succeededFuture(mock(ReadStream.class)));
            } else {
                handler.handle(Future.failedFuture(new NotFoundException()));
            }
        }

        @Override
        public void exists(String id, Handler<AsyncResult<Boolean>> handler) {
            handler.handle(Future.succeededFuture(streams.containsKey(id)));
        }

        @Override
        public void delete(String id, Handler<AsyncResult<Void>> handler) {
            streams.remove(id);
            handler.handle(Future.succeededFuture());
        }

        @Override
        public void get(String id, Handler<AsyncResult<Binary>> handler) {
            if (streams.containsKey(id)) {
                handler.handle(Future.succeededFuture(new ReadStreamBinary(this, id, new Attributes(id, 0, ""))));
            } else {
                handler.handle(Future.failedFuture(new NotFoundException()));
            }
        }
    }
}