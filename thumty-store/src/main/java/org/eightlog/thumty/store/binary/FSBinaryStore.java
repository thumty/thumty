package org.eightlog.thumty.store.binary;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.streams.ReadStream;
import org.eightlog.thumty.common.file.AsyncFileReadStream;
import org.eightlog.thumty.store.Attributes;
import org.eightlog.thumty.store.NotFoundException;
import org.eightlog.thumty.store.binary.utils.StreamProxy;
import org.eightlog.thumty.store.descriptor.Descriptor;
import org.eightlog.thumty.store.descriptor.DescriptorStore;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FSBinaryStore implements BinaryStore {

    private final static String DEFAULT_CONTENT_STORE_BASE_PATH = "./store";
    private final static String DEFAULT_TMP_PATH = "./tmp";

    private Vertx vertx;

    private DescriptorStore descriptors;

    private String basePath;

    private String tmpPath;

    public FSBinaryStore(Vertx vertx, DescriptorStore descriptors) {
        this(vertx, descriptors, DEFAULT_CONTENT_STORE_BASE_PATH, DEFAULT_TMP_PATH);
    }

    public FSBinaryStore(Vertx vertx, DescriptorStore descriptors, String basePath, String tmpPath) {
        Objects.requireNonNull(vertx, "vertx");
        Objects.requireNonNull(descriptors, "descriptors");
        Objects.requireNonNull(basePath, "basePath");
        Objects.requireNonNull(tmpPath, "tmpPath");

        this.vertx = vertx;
        this.descriptors = descriptors;
        this.basePath = basePath;
        this.tmpPath = tmpPath;
    }

    @Override
    public void create(ReadStream<Buffer> content, Handler<AsyncResult<Binary>> handler) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(handler, "handler");

        content.pause();

        Path tempPath = FileSystems.getDefault().getPath(tmpPath, UUID.randomUUID().toString());

        open(tempPath, res -> {
            if (res.succeeded()) {
                AsyncFile temp = res.result();

                StreamProxy.create(content, temp)
                        .exceptionHandler(t -> {
                            vertx.fileSystem().delete(tempPath.toString(), v -> handler.handle(Future.failedFuture(t)));
                        })
                        .endHandler(info -> {
                            temp.close(c -> {
                                Path targetPath = filePath(info.getSha1());
                                Binary binary = binary(info.getSha1(), info);
                                Descriptor descriptor = new Descriptor(info.getSha1(), info.getSize(), info.getContentType());

                                descriptors.create(info.getSha1(), descriptor).compose(references -> {
                                    if (references > 1) {
                                        return remove(tempPath);
                                    } else {
                                        return createFileDirectory(targetPath).compose(v -> move(tempPath, targetPath));
                                    }
                                }).compose(v -> Future.succeededFuture(binary)).setHandler(handler);
                            });
                        });
                content.resume();
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    @Override
    public void duplicate(String id, Handler<AsyncResult<Binary>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        descriptors
                .duplicate(id)
                .compose(n -> n > 0 ? Future.succeededFuture((Void) null) : Future.failedFuture(new NotFoundException("Content with id = " + id + " doesn't exists")))
                .compose(v -> get(id))
                .setHandler(handler);
    }

    @Override
    public void read(String id, Handler<AsyncResult<ReadStream<Buffer>>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        exists(id, res -> {
            if (res.succeeded()) {
                if (res.result()) {
                    vertx.fileSystem().open(filePath(id).toString(), new OpenOptions().setRead(true), r -> {
                        if (r.succeeded()) {
                            handler.handle(Future.succeededFuture(new AsyncFileReadStream(r.result())));
                        } else {
                            handler.handle(Future.failedFuture(r.cause()));
                        }
                    });
                } else {
                    handler.handle(Future.failedFuture(new NotFoundException("Content with id = " + id + " doesn't exists")));
                }
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    @Override
    public void exists(String id, Handler<AsyncResult<Boolean>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        descriptors.exists(id, handler);
    }

    @Override
    public void delete(String id, Handler<AsyncResult<Void>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        descriptors.delete(id).compose(references -> {
            if (references == 0) {
                return remove(filePath(id));
            } else {
                return Future.succeededFuture();
            }
        }).setHandler(handler);
    }

    @Override
    public void get(String id, Handler<AsyncResult<Binary>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        exists(id).compose(exists -> {
            if (exists) {
                return descriptors.read(id).map(d -> binary(id, d));
            } else {
                return Future.failedFuture(new NotFoundException("Content with id = " + id + " doesn't exists"));
            }
        }).setHandler(handler);
    }

    /**
     * Create and open file for writing
     *
     * @param path    the target temp path
     * @param handler AsyncFile handler
     */
    private void open(Path path, Handler<AsyncResult<AsyncFile>> handler) {
        createDirectory(path.getParent(), res -> {
            if (res.succeeded()) {
                vertx.fileSystem().open(path.toString(), new OpenOptions().setWrite(true).setCreate(true), opened -> {
                    if (opened.succeeded()) {
                        handler.handle(Future.succeededFuture(opened.result()));
                    } else {
                        handler.handle(Future.failedFuture(opened.cause()));
                    }
                });
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    /**
     * Move file from path to path, deleting existing file
     *
     * @param from    the from path
     * @param to      the to path
     * @param handler the handler
     */
    private void move(Path from, Path to, Handler<AsyncResult<Void>> handler) {
        Handler<AsyncResult<Void>> onDelete = res -> {
            if (res.succeeded()) {
                vertx.fileSystem().move(from.toString(), to.toString(), handler);
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        };

        Handler<AsyncResult<Boolean>> onExists = res -> {
            if (res.succeeded()) {
                if (res.result()) {
                    vertx.fileSystem().delete(to.toString(), onDelete);
                } else {
                    vertx.fileSystem().move(from.toString(), to.toString(), handler);
                }
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        };

        vertx.fileSystem().exists(to.toString(), onExists);
    }

    /**
     * Move function wrapper, that returns future object
     *
     * @param from the from path
     * @param to   the to path
     * @return the future result
     */
    private Future<Void> move(Path from, Path to) {
        Future<Void> future = Future.future();
        move(from, to, future.completer());
        return future;
    }

    /**
     * Delete file at path future wrapper
     *
     * @param path the file path
     * @return the result future
     */
    private Future<Void> remove(Path path) {
        Future<Void> future = Future.future();
        vertx.fileSystem().delete(path.toString(), future.completer());
        return future;
    }

    /**
     * Create directory at given path
     *
     * @param path    the directory path
     * @param handler the result handler
     */
    private void createDirectory(Path path, Handler<AsyncResult<Void>> handler) {
        vertx.fileSystem().exists(path.toString(), res -> {
            if (res.succeeded()) {
                if (res.result()) {
                    handler.handle(Future.succeededFuture());
                } else {
                    vertx.fileSystem().mkdirs(path.toString(), handler);
                }
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    /**
     * Create parent directories for given path
     *
     * @param path    the file path
     * @param handler the result handler
     */
    private void createFileDirectory(Path path, Handler<AsyncResult<Void>> handler) {
        createDirectory(path.getParent(), handler);
    }

    private Future<Void> createFileDirectory(Path path) {
        Future<Void> future = Future.future();
        createFileDirectory(path, future.completer());
        return future;
    }

    /**
     * Return path for given file id
     *
     * @param id the file id
     * @return a path
     */
    private Path filePath(String id) {
        return fileDir(id).resolve(id);
    }

    /**
     * Return directory path for given file id
     *
     * @param id the file id
     * @return a path
     */
    private Path fileDir(String id) {
        Path root = FileSystems.getDefault().getPath(basePath);

        if (id.length() >= 4) {
            return root.resolve(id.substring(0, 2)).resolve(id.substring(2, 4));
        } else if (id.length() >= 2) {
            return root.resolve(id.substring(0, 2));
        }

        return root;
    }

    private Binary binary(String id, Attributes attributes) {
        return new ReadStreamBinary(this, id, attributes);
    }
}
