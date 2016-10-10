package org.eightlog.thumty.common.file;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class AsyncFileReadStreamTest {

    @Test
    public void shouldCloseFileOnEnd() throws Exception {
        FakeAsyncFile file = new FakeAsyncFile();
        AsyncFileReadStream stream = new AsyncFileReadStream(file);

        file.end();

        assertThat(file.closed).isTrue();
    }


    private static class FakeAsyncFile implements AsyncFile {
        private boolean closed = false;

        private Handler<Void> endHandler;

        @Override
        public AsyncFile handler(Handler<Buffer> handler) {
            return null;
        }

        @Override
        public AsyncFile pause() {
            return null;
        }

        @Override
        public AsyncFile resume() {
            return null;
        }

        @Override
        public AsyncFile endHandler(Handler<Void> endHandler) {
            this.endHandler = endHandler;
            return this;
        }

        @Override
        public AsyncFile write(Buffer data) {
            return null;
        }

        @Override
        public AsyncFile setWriteQueueMaxSize(int maxSize) {
            return null;
        }

        @Override
        public AsyncFile drainHandler(Handler<Void> handler) {
            return null;
        }

        @Override
        public AsyncFile exceptionHandler(Handler<Throwable> handler) {
            return null;
        }

        @Override
        public void end() {
            if (endHandler != null) {
                endHandler.handle(null);
            }
        }

        @Override
        public void close() {
            this.closed = true;
        }

        @Override
        public void close(Handler<AsyncResult<Void>> handler) {
            this.closed = true;
            handler.handle(Future.succeededFuture());
        }

        @Override
        public AsyncFile write(Buffer buffer, long position, Handler<AsyncResult<Void>> handler) {
            return null;
        }

        @Override
        public AsyncFile read(Buffer buffer, int offset, long position, int length, Handler<AsyncResult<Buffer>> handler) {
            return null;
        }

        @Override
        public AsyncFile flush() {
            return null;
        }

        @Override
        public AsyncFile flush(Handler<AsyncResult<Void>> handler) {
            return null;
        }

        @Override
        public AsyncFile setReadPos(long readPos) {
            return null;
        }

        @Override
        public AsyncFile setWritePos(long writePos) {
            return null;
        }

        @Override
        public AsyncFile setReadBufferSize(int readBufferSize) {
            return null;
        }

        @Override
        public boolean writeQueueFull() {
            return false;
        }
    }
}