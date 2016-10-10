package org.eightlog.thumty.loader.fs;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.OpenOptions;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.eightlog.thumty.loader.LoaderException;
import org.eightlog.thumty.store.Attributes;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FileSystemLoader {

    private final static Tika TIKA = new Tika(MimeTypes.getDefaultMimeTypes());

    private final Vertx vertx;

    private final Path basePath;

    private final long expiresIn;

    private Handler<ExpirableAttributedContent> handler;

    private Handler<Throwable> exceptionHandler;

    public FileSystemLoader(Vertx vertx, Path basePath, long expiresIn) {
        this.vertx = vertx;
        this.basePath = basePath;
        this.expiresIn = expiresIn;
    }

    public FileSystemLoader handler(Handler<ExpirableAttributedContent> handler) {
        this.handler = handler;
        return this;
    }

    public FileSystemLoader exceptionHandler(Handler<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public FileSystemLoader load(String uri) {
        Path target = basePath.resolve(uri).normalize();

        if (target.startsWith(basePath)) {
            String path = target.toString();

            vertx.executeBlocking(future -> {
                File file = target.toFile();

                if (file.exists() && file.isFile()) {
                    try {
                        future.complete(new Attributes(null, file.length(), TIKA.detect(file)));
                    } catch (IOException e) {
                        future.fail(e);
                    }

                } else {
                    future.fail(new LoaderException("File not found " + uri));
                }

            }, res -> {
                if (res.succeeded()) {
                    Attributes attributes = (Attributes) res.result();

                    vertx.fileSystem().open(path, new OpenOptions().setRead(true), file -> {
                        if (file.succeeded()) {
                            complete(new AttributedAsyncFileReadStream(file.result(), attributes, getExpiration()));
                        } else {
                            fail(file.cause());
                        }
                    });

                } else {
                    fail(res.cause());
                }
            });
        } else {
            fail(new LoaderException("File not found " + uri));
        }
        return this;
    }

    private LocalDateTime getExpiration() {
        return expiresIn > 0 ? LocalDateTime.now().plus(expiresIn, ChronoUnit.MILLIS) : null;
    }

    private void complete(ExpirableAttributedContent content) {
        if (handler != null) {
            handler.handle(content);
        }
    }

    private void fail(Throwable t) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(t);
        }
    }
}
