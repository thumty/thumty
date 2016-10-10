package org.eightlog.thumty.store.binary.utils;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class StreamProxyTest {

    @Test
    public void shouldProxy() throws Exception {
        FakeReadStream<Buffer> rs = new FakeReadStream<>();
        FakeWriteStream<Buffer> ws = new FakeWriteStream<>();

        StreamProxy proxy = new StreamProxy();
        proxy.proxy(rs, ws);

        List<Buffer> input = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Buffer buffer = Buffer.buffer("0123456789");
            input.add(buffer);
            rs.addData(buffer);
        }

        assertThat(ws.received).isEqualTo(input);
    }

    @Test
    public void shouldGetContentInfo() throws Exception {
        FakeReadStream<Buffer> rs = new FakeReadStream<>();
        FakeWriteStream<Buffer> ws = new FakeWriteStream<>();

        StreamProxy proxy = new StreamProxy().endHandler(i -> {
            assertThat(i.getSize()).isEqualTo(100);
            assertThat(i.getContentType()).isEqualTo("text/plain");
            assertThat(i.getSha1()).isEqualTo("29b0e7878271645fffb7eec7db4a7473a1c00bc1");
        });

        proxy.proxy(rs, ws);

        List<Buffer> input = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Buffer buffer = Buffer.buffer("0123456789");
            input.add(buffer);
            rs.addData(buffer);
        }

        rs.end();
        assertThat(ws.received).isEqualTo(input);

    }

    private class FakeReadStream<T> implements ReadStream<T> {

        private Handler<T> dataHandler;
        private Handler<Void> endHandler;
        private boolean paused;
        int pauseCount;
        int resumeCount;

        void addData(T data) {
            if (dataHandler != null) {
                dataHandler.handle(data);
            }
        }

        void end() {
            endHandler.handle(null);
        }

        public FakeReadStream handler(Handler<T> handler) {
            this.dataHandler = handler;
            return this;
        }

        public FakeReadStream pause() {
            paused = true;
            pauseCount++;
            return this;
        }

        public FakeReadStream pause(Handler<Void> doneHandler) {
            pause();
            doneHandler.handle(null);
            return this;
        }

        public FakeReadStream resume() {
            paused = false;
            resumeCount++;
            return this;
        }

        public FakeReadStream resume(Handler<Void> doneHandler) {
            resume();
            doneHandler.handle(null);
            return this;
        }

        public FakeReadStream exceptionHandler(Handler<Throwable> handler) {
            return this;
        }

        public FakeReadStream endHandler(Handler<Void> endHandler) {
            this.endHandler = endHandler;
            return this;
        }
    }

    private class FakeWriteStream<T> implements WriteStream<T> {

        int maxSize;
        List<T> received = new ArrayList<T>();
        Handler<Void> drainHandler;

        void clearReceived() {
            boolean callDrain = writeQueueFull();
            received = new ArrayList<>();
            if (callDrain && drainHandler != null) {
                drainHandler.handle(null);
            }
        }

        public FakeWriteStream setWriteQueueMaxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public boolean writeQueueFull() {
            return received.size() >= maxSize;
        }

        public FakeWriteStream drainHandler(Handler<Void> handler) {
            this.drainHandler = handler;
            return this;
        }

        public FakeWriteStream write(T data) {
            received.add(data);
            return this;
        }

        public FakeWriteStream exceptionHandler(Handler<Throwable> handler) {
            return this;
        }

        @Override
        public void end() {
        }
    }
}