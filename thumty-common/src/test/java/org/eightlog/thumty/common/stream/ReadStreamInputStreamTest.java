package org.eightlog.thumty.common.stream;

import com.google.common.io.ByteStreams;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
@RunWith(VertxUnitRunner.class)
public class ReadStreamInputStreamTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldRead(TestContext ctx) throws Exception {
        Async async = ctx.async();

        FakeReadStream readStream = new FakeReadStream(10);

        rule.vertx().executeBlocking(f -> {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                try (InputStream in = new ReadStreamInputStream(rule.vertx(), readStream)) {
                    ByteStreams.copy(in, out);
                }
                f.complete(out.toByteArray());
            } catch (IOException e) {
                f.fail(e);
            }

        }, res -> {
            async.complete();
            ctx.assertTrue(res.succeeded());
            byte[] result = (byte[]) res.result();

            ctx.assertEquals(result.length, 64 * 1024 * 16);

            for (int j = 0; j < 64; j++) {
                for (int i = 0; i < 1024 * 16; i++) {
                    ctx.assertEquals(result[j * 1024 * 16 + i], (byte) (i % 256));
                }
            }
        });

        for (int j = 0; j < 64; j++) {
            byte[] data = new byte[1024 * 16];

            for (int i = 0; i < 1024 * 16; i++) {
                data[i] = (byte) (i % 256);
            }
            readStream.write(data);
        }
        readStream.end();
    }

    @Test
    public void shouldFailOnTimeout(TestContext ctx) throws Exception {
        Async async = ctx.async();

        FakeReadStream readStream = new FakeReadStream(200);

        rule.vertx().executeBlocking(f -> {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                try (InputStream in = new ReadStreamInputStream(rule.vertx(), readStream, 100, TimeUnit.MILLISECONDS)) {
                    ByteStreams.copy(in, out);
                }
                f.complete(out.toByteArray());
            } catch (IOException e) {
                f.fail(e);
            }

        }, res -> {
            async.complete();
            ctx.assertTrue(res.failed());

        });

        readStream.write(new byte[]{0x00, 0x01, 0x02});
        readStream.write(new byte[]{0x00, 0x01, 0x02});
        readStream.end();
    }

    @Test
    public void shouldFailOnException(TestContext ctx) throws Exception {
        Async async = ctx.async();

        FakeReadStream readStream = new FakeReadStream(10);

        rule.vertx().executeBlocking(f -> {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                try (InputStream in = new ReadStreamInputStream(rule.vertx(), readStream)) {
                    ByteStreams.copy(in, out);
                }
                f.complete(out.toByteArray());
            } catch (IOException e) {
                f.fail(e);
            }

        }, res -> {
            ctx.assertTrue(res.failed());
            async.complete();
        });

        readStream.write(new byte[]{0x00, 0x01, 0x02});
        readStream.error(new Exception("Error"));
    }

    private class FakeReadStream implements ReadStream<Buffer> {

        private Handler<Throwable> exceptionHandler;
        private Handler<Buffer> handler;
        private Handler<Void> endHandler;

        private Deque<Buffer> deque = new ArrayDeque<>();

        private boolean paused;

        private boolean ended;

        public FakeReadStream(long delay) {
            rule.vertx().setPeriodic(delay, v -> {
                if (ended && deque.size() == 0) {
                    rule.vertx().cancelTimer(v);

                    endHandler.handle(null);
                }

                if (!paused && deque.size() > 0) {
                    if (handler != null) {
                        handler.handle(deque.pollLast());
                    }
                }
            });
        }

        @Override
        public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
            this.exceptionHandler = handler;
            return this;
        }

        @Override
        public ReadStream<Buffer> handler(Handler<Buffer> handler) {
            this.handler = handler;
            return this;
        }

        @Override
        public ReadStream<Buffer> pause() {
            paused = true;
            return this;
        }

        @Override
        public ReadStream<Buffer> resume() {
            paused = false;
            return this;
        }

        @Override
        public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
            this.endHandler = endHandler;
            return this;
        }

        public void write(byte[] bytes) {
            deque.add(Buffer.buffer(bytes));
        }

        public void end() {
            ended = true;
        }

        public void error(Throwable error) {
            if (exceptionHandler != null) {
                exceptionHandler.handle(error);
            }
        }
    }
}