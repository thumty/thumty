package org.eightlog.thumty.loader.fs;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eightlog.thumty.loader.LoaderException;
import org.eightlog.thumty.store.ExpirableAttributedContent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class FileSystemLoaderTest {
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldReadContent(TestContext ctx) throws Exception {
        Async async = ctx.async();
        Vertx vertx = rule.vertx();

        FileSystemLoader loader = new FileSystemLoader(vertx, Paths.get("./src/test/resources").toRealPath(), 0);

        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                Handler<ExpirableAttributedContent> handler = content -> {
                    ctx.assertNotNull(content.getAttributes().getContentType(), "should detect content type");
                    ctx.assertTrue(content.getAttributes().getSize() > 0, "should detect content size");
                    ctx.assertNull(content.getExpires());

                    async.complete();
                };

                loader.exceptionHandler(ctx::fail).handler(handler).load("image.jpg");
            }
        });
    }

    @Test
    public void shouldFailIfFileDoesntExists(TestContext ctx) throws Exception {
        Async async = ctx.async();
        Vertx vertx = rule.vertx();

        FileSystemLoader loader = new FileSystemLoader(vertx, Paths.get("./src/test/resources").toRealPath(), 0);

        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                Handler<ExpirableAttributedContent> handler = content -> {
                    ctx.fail("should not open content");
                };

                Handler<Throwable> fail = t -> {
                    ctx.assertTrue(t instanceof LoaderException);
                    async.complete();
                };

                loader.exceptionHandler(fail).handler(handler).load("unknown.jpg");
            }
        });
    }

    @Test
    public void shouldFailOnInvalidRelativePath(TestContext ctx) throws Exception {
        Async async = ctx.async();
        Vertx vertx = rule.vertx();

        FileSystemLoader loader = new FileSystemLoader(vertx, Paths.get("./src/test/resources").toRealPath(), 0);

        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                Handler<ExpirableAttributedContent> handler = content -> {
                    ctx.fail("should not open content");
                };

                Handler<Throwable> fail = t -> {
                    ctx.assertTrue(t instanceof LoaderException);
                    async.complete();
                };

                loader.exceptionHandler(fail).handler(handler).load("../image.gif");
            }
        });
    }

    @Test
    public void shouldOpenRelativePath(TestContext ctx) throws Exception {
        Async async = ctx.async();
        Vertx vertx = rule.vertx();

        FileSystemLoader loader = new FileSystemLoader(vertx, Paths.get("./src/test/resources").toRealPath(), 0);

        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                Handler<ExpirableAttributedContent> handler = content -> {
                    ctx.assertNotNull(content.getAttributes().getContentType(), "should detect content type");
                    ctx.assertTrue(content.getAttributes().getSize() > 0, "should detect content size");

                    async.complete();
                };

                loader.exceptionHandler(ctx::fail).handler(handler).load("sub/../image.gif");
            }
        });
    }

    @Test
    public void shouldSetExpiration(TestContext ctx) throws Exception {
        Async async = ctx.async();
        Vertx vertx = rule.vertx();

        FileSystemLoader loader = new FileSystemLoader(vertx, Paths.get("./src/test/resources").toRealPath(), TimeUnit.HOURS.toMillis(10));

        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                Handler<ExpirableAttributedContent> handler = content -> {
                    LocalDateTime now = LocalDateTime.now();

                    ctx.assertTrue(content.getExpires().minusSeconds(10).isBefore(now.plusHours(10)));
                    ctx.assertTrue(content.getExpires().plusSeconds(10).isAfter(now.plusHours(10)));

                    async.complete();
                };

                loader.exceptionHandler(ctx::fail).handler(handler).load("image.jpg");
            }
        });
    }
}