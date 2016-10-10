package org.eightlog.thumty.common.stream;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class WriteStreamOutputStreamTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldWriteBytes(TestContext ctx) throws Exception {
        Async async = ctx.async();

        FakeWriteStream writeStream = new FakeWriteStream();

        rule.vertx().executeBlocking(f -> {
            try (OutputStream output = new WriteStreamOutputStream(writeStream)) {
                for (int i = 0; i < 1024 * 64; i++) {
                    output.write(i);
                }
                f.complete();
            } catch (IOException e) {
                ctx.fail(e);
            }
        }, res -> {

            ctx.assertEquals(writeStream.content.size(), 1024 * 64);
            ctx.assertTrue(writeStream.end);

            for (int i = 0; i < writeStream.content.size(); i++) {
                ctx.assertEquals(writeStream.content.get(i), (byte) (i % 256));
            }

            async.complete();
        });
    }

    @Test
    public void shouldHandleQueueFull(TestContext ctx) throws Exception {
        Async async = ctx.async();

        FakeWriteStream writeStream = new FakeWriteStream(500).setWriteQueueMaxSize(1024);

        rule.vertx().executeBlocking(f -> {
            try (OutputStream output = new WriteStreamOutputStream(writeStream, 1000, TimeUnit.MILLISECONDS)) {
                for (int i = 0; i < 1024 * 64; i++) {
                    output.write(i);
                }
                f.complete();
            } catch (IOException e) {
                ctx.fail(e);
            }
        }, res -> {
            ctx.assertEquals(writeStream.content.size(), 1024 * 64);
            ctx.assertTrue(writeStream.end);

            for (int i = 0; i < writeStream.content.size(); i++) {
                ctx.assertEquals(writeStream.content.get(i), (byte) (i % 256));
            }

            async.complete();
        });
    }

    @Test
    public void shouldFailOnTimeout(TestContext ctx) throws Exception {
        Async async = ctx.async();

        FakeWriteStream writeStream = new FakeWriteStream(500).setWriteQueueMaxSize(1024);

        rule.vertx().executeBlocking(f -> {
            try (OutputStream output = new WriteStreamOutputStream(writeStream, 500, TimeUnit.MILLISECONDS)) {
                for (int i = 0; i < 1024 * 64; i++) {
                    output.write(i);
                }
                f.complete();
            } catch (IOException e) {
                f.fail(e);
            }
        }, res -> {
            ctx.assertTrue(res.failed());
            async.complete();
        });

    }

    private class FakeWriteStream implements WriteStream<Buffer> {
        private ArrayList<Byte> content = new ArrayList<>();
        private Handler<Void> drainHandler;
        private boolean end;
        private boolean queueFull;

        private int queueMaxSize = 1024;
        private int queued;
        private long delay;

        public FakeWriteStream() {
        }

        public FakeWriteStream(long delay) {
            this.delay = delay;
        }

        @Override
        public FakeWriteStream exceptionHandler(Handler<Throwable> handler) {
            return this;
        }

        @Override
        public FakeWriteStream write(Buffer data) {
            for (int i = 0; i < data.length(); i++) {
                content.add(data.getByte(i));
            }
            queued += data.length();

            if (queued > queueMaxSize && delay > 0) {
                queueFull = true;

                rule.vertx().setTimer(delay, event -> {
                    queueFull = false;
                    queued = 0;

                    if (drainHandler != null) {
                        drainHandler.handle(null);
                    }
                });
            }

            return this;
        }

        @Override
        public void end() {
            end = true;
        }

        @Override
        public FakeWriteStream setWriteQueueMaxSize(int maxSize) {
            this.queueMaxSize = maxSize;
            return this;
        }

        @Override
        public boolean writeQueueFull() {
            return queueFull;
        }

        @Override
        public FakeWriteStream drainHandler(Handler<Void> handler) {
            this.drainHandler = handler;
            return this;
        }
    }
}