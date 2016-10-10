package org.eightlog.thumty.store.content;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class ContentStoreFactoryTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldInitializeFileStore(TestContext ctx) throws Exception {
        Async async = ctx.async();
        async.complete();

//        rule.vertx().deployVerticle(new AbstractVerticle() {
//            @Override
//            public void start() throws Exception {
//                JDBCClient client = JDBCClient.createShared(vertx, new JsonObject()
//                        .put("url", "jdbc:h2:mem:db")
//                        .put("driver_class", "org.h2.Driver"));
//
//                ContentStore store = ContentStoreFactory.create(vertx, client, "test", "./");
//
//                vertx.fileSystem().open("./src/test/resources/sample.jpg", new OpenOptions().setRead(true), f -> {
//                    store.write("sample.jpg", f.result(), res -> {
//                        ctx.assertTrue(res.succeeded());
//                        ctx.assertNotNull(res.result());
//                        async.complete();
//                    });
//                });
//            }
//        });

    }
}